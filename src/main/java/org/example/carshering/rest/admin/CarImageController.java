package org.example.carshering.rest.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.response.ImageResponse;
import org.example.carshering.domain.entity.Image;
import org.example.carshering.service.interfaces.ImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Car Image Management", description = "Endpoints for managing car images (admin access)")
public class CarImageController {
    private final ImageService imageService;

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload car image",
            description = "Upload an image file for a specific car. Accepts image files in common formats (JPEG, PNG, etc.)"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Image uploaded successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ImageResponse.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid file format or file is too large"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Car not found"
    )
    public ResponseEntity<ImageResponse> uploadImage(
            @Parameter(
                    description = "ID of the car to upload image for",
                    example = "1",
                    required = true
            ) @PathVariable Long id,
            @Parameter(
                    description = "Image file to upload",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            ) @RequestParam("file") MultipartFile file
    ) {
        Image img = imageService.upload(id, file);
        return ResponseEntity.ok(
                new ImageResponse(img.getId(), img.getUrl())
        );
    }
}
