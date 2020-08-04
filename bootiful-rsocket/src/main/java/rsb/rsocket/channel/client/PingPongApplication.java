package rsb.rsocket.channel.client;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PingPongApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(PingPongApplication.class, args);
		System.in.read();
	}

}
