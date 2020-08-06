package rsb.rsocket.security.client;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Log4j2
@SpringBootApplication
public class SecurityApplication {

	@SneakyThrows
	public static void main(String args[]) {
		SpringApplication.run(SecurityApplication.class, args);
		System.in.read();
	}

}
