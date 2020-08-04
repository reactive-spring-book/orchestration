package rsb.rsocket.routing.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RoutingApplication {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "service");
		SpringApplication.run(RoutingApplication.class, args);
	}

}
