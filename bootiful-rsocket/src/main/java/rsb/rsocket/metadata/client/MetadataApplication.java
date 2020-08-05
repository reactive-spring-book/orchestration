package rsb.rsocket.metadata.client;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class MetadataApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(MetadataApplication.class, args);
		System.in.read();
	}

}
