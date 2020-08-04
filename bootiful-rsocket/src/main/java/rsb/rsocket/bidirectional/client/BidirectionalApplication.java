package rsb.rsocket.bidirectional.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BidirectionalApplication {

	public static void main(String[] args) {
		System.setProperty("spring.rsocket.server.port", "9090");
		SpringApplication.run(BidirectionalApplication.class, args);
	}

}