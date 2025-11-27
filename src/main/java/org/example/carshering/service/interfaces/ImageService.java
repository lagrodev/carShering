package org.example.carshering.service.interfaces;

import org.example.carshering.entity.Image;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    Image upload(Long carId, MultipartFile file);
}
