package rsb.rsocket.encoding.client;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EncodingApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(EncodingApplication.class, args);
		System.in.read();
	}

}
