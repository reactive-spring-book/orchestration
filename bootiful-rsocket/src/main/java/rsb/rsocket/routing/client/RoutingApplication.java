package rsb.rsocket.routing.client;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RoutingApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(RoutingApplication.class, args);
		System.in.read();
	}

}
