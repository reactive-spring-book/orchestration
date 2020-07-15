package rsb.rsocket.metadata.service;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MetadataApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(MetadataApplication.class, args);
		System.in.read();
	}

}
