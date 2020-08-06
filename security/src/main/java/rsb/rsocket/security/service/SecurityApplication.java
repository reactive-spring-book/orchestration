package rsb.rsocket.security.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SecurityApplication {

	public static void main(String args[]) {
		System.setProperty("spring.profiles.active", "service");
		SpringApplication.run(SecurityApplication.class, args);
	}

}
