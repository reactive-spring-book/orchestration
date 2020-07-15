package rsb.rsocket.setup.client;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.RSocketRequester;
import rsb.rsocket.BootifulProperties;

@SpringBootApplication
public class SetupApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(SetupApplication.class, args);
		System.in.read();
	}

	@Bean
	RSocketRequester rSocketRequester(BootifulProperties properties,
			RSocketRequester.Builder builder) {
		return builder.setupData("setup data!").setupRoute("setup")
				.connectTcp(properties.getRsocket().getHostname(),
						properties.getRsocket().getPort())
				.block();
	}

}
