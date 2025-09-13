package com.jordi.booknook.security;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.graph.authentication.TokenCredentialAuthProvider;
import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class GraphConfig {

    @Value("${azure.tenant-id}")        String tenantId;
    @Value("${azure.admin.client-id}")  String adminClientId;
    @Value("${azure.admin.client-secret}") String adminClientSecret;

    @Bean
    public GraphServiceClient<Request> graphClient() {
        ClientSecretCredential cred = new ClientSecretCredentialBuilder()
                .tenantId(tenantId)
                .clientId(adminClientId)
                .clientSecret(adminClientSecret)
                .build();

        // Scope .default tells AAD to issue Graph app-only tokens according to consented app perms
        var authProvider = new TokenCredentialAuthProvider(
                List.of("https://graph.microsoft.com/.default"),
                cred
        );

        return GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();
    }
}
