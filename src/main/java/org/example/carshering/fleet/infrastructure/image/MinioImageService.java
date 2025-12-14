package org.example.carshering.fleet.infrastructure.image;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.carshering.fleet.domain.valueobject.FileName;
import org.example.carshering.fleet.domain.valueobject.ImageData;
import org.example.carshering.fleet.domain.valueobject.ImageUrl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

/**
 * Реализация ImageService с использованием MinIO
 *
 * Отвечает ТОЛЬКО за работу с файловым хранилищем (MinIO)

 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioImageService implements ImageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Override
    public ImageData uploadToStorage(Long carId, MultipartFile file) {
        try {
            // 1. Генерируем уникальное имя файла
            String ext = Objects.requireNonNull(file.getOriginalFilename())
                    .substring(file.getOriginalFilename().lastIndexOf("."));
            String objectName = carId + "/" + UUID.randomUUID() + ext;

            log.info("Uploading file to MinIO: {}", objectName);

            // 2. Загружаем в MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .contentType(file.getContentType())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build()
            );

            // 3. Получаем публичный URL
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .method(Method.GET)
                            .build()
            );

            log.info("File uploaded successfully: {}", objectName);

            // 4. Возвращаем Value Object (НЕ Entity!)
            return ImageData.create(
                    new FileName(objectName),
                    new ImageUrl(url)
            );

        } catch (Exception e) {
            log.error("Failed to upload file to MinIO", e);
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }

    @Override
    public void deleteFromStorage(String fileName) {
        try {
            log.info("Deleting file from MinIO: {}", fileName);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(fileName)
                            .build()
            );

            log.info("File deleted successfully: {}", fileName);

        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: {}", fileName, e);
            throw new RuntimeException("Failed to delete file from MinIO", e);
        }
    }

    @Override
    public String getPublicUrl(String fileName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucket)
                            .object(fileName)
                            .method(Method.GET)
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get URL from MinIO: {}", fileName, e);
            throw new RuntimeException("Failed to get URL from MinIO", e);
        }
    }
}
