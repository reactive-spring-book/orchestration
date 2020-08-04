package rsb.rsocket.fireandforget.client;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FireAndForgetApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(FireAndForgetApplication.class, args);
		System.in.read();
	}

}
