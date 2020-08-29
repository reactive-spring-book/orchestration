package rsb.orchestration.resilience4j;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ResilientClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ResilientClientApplication.class, args);
	}

}