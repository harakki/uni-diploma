package dev.harakki.comics;

import org.springframework.boot.SpringApplication;

public class TestComicsApplication {

    static void main(String[] args) {
        SpringApplication.from(ComicsApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
