package rsb.rsocket.errors.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ErrorApplication {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "service");
		SpringApplication.run(ErrorApplication.class, args);
	}

}
