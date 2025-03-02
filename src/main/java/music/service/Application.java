package music.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(final String[] args) {

        System.out.println("DB_PASSWORD: " + System.getenv("DB_PASSWORD"));

        SpringApplication.run(Application.class, args);
    }
}