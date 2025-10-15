package org.example.carshering;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication

public class CarSheringApplication {

	public static void main(String[] args) {

		SpringApplication.run(CarSheringApplication.class, args);
	}

}
