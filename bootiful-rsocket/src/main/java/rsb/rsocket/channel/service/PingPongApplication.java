package rsb.rsocket.channel.service;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PingPongApplication {

	@SneakyThrows
	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "service");
		SpringApplication.run(PingPongApplication.class, args);
	}

}
