package dev.harakki.comics;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer("postgres:latest");
    }

    @Bean
    MinIOContainer minioContainer() {
        var container = new MinIOContainer("minio/minio:latest")
                .withEnv("MINIO_ROOT_USER", "minioadmin")
                .withEnv("MINIO_ROOT_PASSWORD", "minioadmin");
        container.start();

        System.setProperty("s3.endpoint", container.getS3URL());
        System.setProperty("s3.access-key", container.getUserName());
        System.setProperty("s3.secret-key", container.getPassword());
        System.setProperty("s3.region", "us-east-1");
        System.setProperty("s3.bucket", "comics-bucket");

        return container;
    }

}
