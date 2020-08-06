package rsb.rsocket.setup.client;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SetupApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(SetupApplication.class, args);
		System.in.read();
	}

}
