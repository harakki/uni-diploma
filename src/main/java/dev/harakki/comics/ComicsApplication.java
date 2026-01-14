package dev.harakki.comics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableRetry
@SpringBootApplication
public class ComicsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComicsApplication.class, args);
    }

}
