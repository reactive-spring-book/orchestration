package rsb.rsocket.requestresponse.client;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.RSocketRequester;
import rsb.rsocket.BootifulProperties;

@SpringBootApplication
public class RequestResponseApplication {

	@SneakyThrows
	public static void main(String[] arrrImAPirate) {
		SpringApplication.run(RequestResponseApplication.class, arrrImAPirate);
		System.in.read();
	}

	@Bean
	RSocketRequester rSocketRequester(BootifulProperties properties,
			RSocketRequester.Builder builder) {
		return builder//
				.connectTcp(properties.getRsocket().getHostname(),
						properties.getRsocket().getPort())//
				.block();
	}

}
