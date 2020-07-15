package rsb.rsocket.channel.service;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Handles channel requests. Run this first.
 */
@SpringBootApplication
public class PingPongApplication {

	static String reply(String request) {
		return request.equalsIgnoreCase("ping") ? "pong" : "ping";
	}

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(PingPongApplication.class, args);
		System.in.read();
	}

}
