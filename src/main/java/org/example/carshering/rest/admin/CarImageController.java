package org.example.carshering.rest.admin;

import lombok.RequiredArgsConstructor;
import org.example.carshering.dto.response.ImageResponse;
import org.example.carshering.entity.Image;
import org.example.carshering.service.interfaces.ImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class CarImageController {
    private final ImageService imageService;

    @PostMapping("/{id}/images")
    public ResponseEntity<ImageResponse> uploadImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file
    ) {
        Image img = imageService.upload(id, file);
        return ResponseEntity.ok(
                new ImageResponse(img.getId(), img.getUrl())
        );
    }
}
