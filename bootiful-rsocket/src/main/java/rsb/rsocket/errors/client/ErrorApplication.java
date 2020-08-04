package rsb.rsocket.errors.client;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ErrorApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(ErrorApplication.class, args);
		System.in.read();
	}

}
