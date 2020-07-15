package rsb.rsocket.fireandforget.client;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.RSocketRequester;
import rsb.rsocket.BootifulProperties;

@SpringBootApplication
public class FireAndForgetApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(FireAndForgetApplication.class, args);
		System.in.read();
	}

	@Bean
	RSocketRequester rSocketRequester(BootifulProperties properties,
			RSocketRequester.Builder builder) {
		return builder.connectTcp(properties.getRsocket().getHostname(),
				properties.getRsocket().getPort()).block();
	}

}
