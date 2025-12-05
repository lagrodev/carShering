package org.example.carshering.service.impl;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.example.carshering.domain.entity.Car;
import org.example.carshering.domain.entity.Image;
import org.example.carshering.repository.CarRepository;
import org.example.carshering.repository.ImageRepository;
import org.example.carshering.service.interfaces.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioImageService implements ImageService {
    private final MinioClient minioClient;
    private final ImageRepository imageRepository;
    private final CarRepository carRepository;

    @Value("${minio.bucket}")
    private String bucket;

    @Override
    public Image upload(Long carId, MultipartFile file) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("Car not found"));

        try {
            // имя файла
            String ext = Objects.requireNonNull(file.getOriginalFilename())
                    .substring(file.getOriginalFilename().lastIndexOf("."));
            String objectName = carId + "/" + UUID.randomUUID() + ext;

            // загружаем в MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .contentType(file.getContentType())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build()
            );

            // формируем публичный URL
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .method(Method.GET)
                            .build()
            );

            // сохраняем в БД
            Image img = Image.builder()
                    .fileName(objectName)
                    .url(url)
                    .car(car)
                    .build();

            return imageRepository.save(img);

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file", e);
        }
    }
}
