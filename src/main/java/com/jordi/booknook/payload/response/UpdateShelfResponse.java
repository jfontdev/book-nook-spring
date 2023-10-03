package com.jordi.booknook.payload.response;


public record UpdateShelfResponse(
       String username,
       Long shelf_id,
       String shelf_name,
       String shelf_image,
       String shelf_description,
       Boolean public_shelf
) {
}
