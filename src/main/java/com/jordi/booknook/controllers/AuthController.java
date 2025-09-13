package com.jordi.booknook.controllers;

import com.jordi.booknook.security.payload.request.LoginRequest;
import com.microsoft.aad.msal4j.*;
import com.microsoft.graph.models.PasswordProfile;
import com.microsoft.graph.models.User;
import com.microsoft.graph.requests.GraphServiceClient;
import com.jordi.booknook.models.UserEntity;
import com.jordi.booknook.repositories.UserRepository;
import com.jordi.booknook.security.payload.request.RegisterRequest;
import com.jordi.booknook.security.payload.response.MessageResponse;
import jakarta.validation.Valid;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final UserRepository users;
    private final GraphServiceClient<Request> graph;

    public AuthController(UserRepository users, GraphServiceClient<Request> graph) {
        this.users = users;
        this.graph = graph;
    }

    @Value("${azure.tenant-id}")
    String tenantId;
    @Value("${azure.api-app-id}")
    String apiAppId;
    @Value("${azure.tenant-domain}")
    String tenantDomain;
    @Value("${azure.ropc.client-id}")
    String ropcClientId;

    /**
     * Endpoint to login via EntraID
     * @param req
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {
        try {
            // 1) Public client (NO secret) for ROPC
            PublicClientApplication pca = PublicClientApplication.builder(ropcClientId)
                    .authority("https://login.microsoftonline.com/" + tenantId + "/") // v2.0 is inferred
                    .build();

            // 2) Scopes: your API scopes + OIDC bits
            Set<String> scopes = new LinkedHashSet<>(List.of(
                    "api://" + apiAppId + "/books.read",
                    "api://" + apiAppId + "/books.write",
                    "openid", "profile", "email", "offline_access"
            ));

            // 3) Acquire token by username/password
            UserNamePasswordParameters params = UserNamePasswordParameters
                    .builder(scopes, req.username(), req.password().toCharArray())
                    .build();

            IAuthenticationResult result = pca.acquireToken(params).join();

            // 4) Build a friendly body. MSAL doesn’t expose refresh_token.
            Instant exp = result.expiresOnDate().toInstant();
            long expiresIn = Duration.between(Instant.now(), exp).getSeconds();
            if (expiresIn < 0) expiresIn = 0;

            // Parse a few claims from id_token for convenience
            Map<String, Object> idClaims = Map.of();
            try {
                var jwt = com.nimbusds.jwt.SignedJWT.parse(result.idToken());
                idClaims = jwt.getJWTClaimsSet().getClaims();
            } catch (Exception ignore) { /* optional */ }

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("token_type", "Bearer");
            body.put("access_token", result.accessToken());
            body.put("expires_in", expiresIn);
            body.put("scope", String.join(" ", scopes));
            body.put("id_token", result.idToken());
            body.put("id_claims", idClaims); // contains sub, oid, name, preferred_username, etc.

            return ResponseEntity.ok(body);

        } catch (CompletionException ce) {
            Throwable cause = ce.getCause() != null ? ce.getCause() : ce;
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Login failed: " + cause.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Login failed: " + ex.getMessage()));
        }
    }

    /**
     * Endpoint to register a user in Entra ID
     * @param req - The request body
     * @return - A success message or an error
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        try {
            // --- 1) sanitize/derive Graph fields from your DTO ---
            String sanitized = req.username().trim().toLowerCase().replaceAll("[^a-z0-9._-]", "");
            if (sanitized.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "invalid_username",
                        "detail", "Username must contain letters, digits, dot, dash or underscore."
                ));
            }

            String upn = sanitized + "@" + tenantDomain; // e.g., yourtenant.onmicrosoft.com

            // --- 2) build Graph user payload ---
            var pwd = new PasswordProfile();
            pwd.forceChangePasswordNextSignIn = false;     // for your learning app
            pwd.password = req.password();                 // must meet tenant password policy

            var user = new User();
            user.accountEnabled = true;
            user.displayName = sanitized;                  // or use something prettier
            user.mailNickname = sanitized;
            user.userPrincipalName = upn;
            user.passwordProfile = pwd;

            // Optional: keep the email the user typed so you can contact them later
            if (req.email() != null && !req.email().isBlank()) {
                user.otherMails = new LinkedList<>();
                user.otherMails.add(req.email());
            }

            // Optional: sometimes required for license assignment flows
             user.usageLocation = "ES";

            // --- 3) create in Entra with Graph SDK (app-only) ---
            User created = graph.users().buildRequest().post(user);

            // --- 4) upsert your local profile (NO password) ---
            var entraId = created.id; // Graph returned id (also matches JWT 'oid' for users)
            UserEntity userEntity = users.findByUsername(sanitized)
                    .orElseGet(() -> UserEntity.fromEntra(sanitized, req.email(), entraId));
            userEntity.setAuthProvider("entra");
            userEntity.setAuthSub(entraId);
            users.save(userEntity);

            return ResponseEntity.status(201).body(Map.of(
                    "message", "User created",
                    "entra_id", created.id,
                    "userPrincipalName", created.userPrincipalName
            ));

        } catch (com.microsoft.graph.core.ClientException ex) {
            // Map common Graph errors to something friendlier
            String msg = ex.getMessage();
            int status = 400;
            if (msg != null && msg.contains("Request_ResourceAlreadyExists")) {
                status = 409;
            } else if (msg != null && msg.contains("PasswordTooShort")) {
                status = 422;
                msg = "Password does not meet the tenant policy requirements.";
            }
            return ResponseEntity.status(status).body(Map.of(
                    "error", "graph_error",
                    "detail", msg
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "register_failed",
                    "detail", e.getMessage()
            ));
        }
    }

    /**
     * Returns info about the caller derived from the validated JWT
     * + an optional local profile (if you've linked Entra ID to your DB row).
     *
     * Works for both delegated (user) tokens (scp claim)
     * and client-credentials tokens (roles claim).
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        // --- Common OIDC / Entra claims ---
        String sub = jwt.getSubject();                        // stable subject
        String oid = jwt.getClaimAsString("oid");             // object id for users/apps
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        String name = jwt.getClaimAsString("name");
        String email = jwt.getClaimAsString("email");         // may be null
        String issuer = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
        List<String> aud = jwt.getAudience();

        // --- Scopes / Roles (resource permissions) ---
        // For delegated user tokens from v2 endpoint
        Object scp = jwt.getClaims().get("scp");              // space-delimited string or null
        List<String> scopes = (scp instanceof String s && !s.isBlank())
                ? Arrays.asList(s.split("\\s+"))
                : List.of();

        // For client credentials tokens with app roles (a.k.a. "roles" claim)
        @SuppressWarnings("unchecked")
        List<String> appRoles = (List<String>) jwt.getClaims().getOrDefault("roles", List.of());

        // --- Times ---
        Instant iat = jwt.getIssuedAt();
        Instant exp = jwt.getExpiresAt();

        // --- Try to resolve local profile using Entra identifier ---
        String identityKey = (oid != null && !oid.isBlank()) ? oid : sub;
        var localProfile = users.findByAuthSub(identityKey)
                .map(u -> Map.of(
                        "id", u.getUser_id(),
                        "username", u.getUsername(),
                        "email", u.getEmail(),
                        "auth_provider", u.getAuthProvider(),
                        "auth_sub", u.getAuthSub()
                ))
                .orElse(null);

        // --- Response payload ---
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("subject", sub);
        body.put("oid", oid);
        body.put("preferred_username", preferredUsername);
        body.put("name", name);
        body.put("email", email);
        body.put("issuer", issuer);
        body.put("audience", aud);
        body.put("scopes", scopes);
        body.put("roles", appRoles);
        body.put("issued_at", iat);
        body.put("expires_at", exp);
        if (localProfile != null) body.put("profile", localProfile);

        return ResponseEntity.ok(body);
    }
}
