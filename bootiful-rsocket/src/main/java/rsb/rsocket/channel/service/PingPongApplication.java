package rsb.rsocket.channel.service;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PingPongApplication {

	static String reply(String request) {
		return request.equalsIgnoreCase("ping") ? "pong" : "ping";
	}

	@SneakyThrows
	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "service");
		SpringApplication.run(PingPongApplication.class, args);
	}

}
