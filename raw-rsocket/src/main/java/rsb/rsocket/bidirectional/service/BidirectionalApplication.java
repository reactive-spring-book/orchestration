package rsb.rsocket.bidirectional.service;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BidirectionalApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(BidirectionalApplication.class, args);
		System.in.read();
	}

}
