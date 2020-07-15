package rsb.rsocket.metadata.client;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeTypeUtils;
import rsb.rsocket.BootifulProperties;

import java.util.UUID;

@SpringBootApplication
@RequiredArgsConstructor
public class MetadataApplication {

	private final BootifulProperties properties;

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(MetadataApplication.class, args);
		System.in.read();
	}

	@Bean
	Client client2(RSocketRequester.Builder builder) {
		return this.buildClient(builder);
	}

	@Bean
	Client client1(RSocketRequester.Builder builder) {
		return this.buildClient(builder);
	}

	private RSocketRequester rsocketRequester(RSocketRequester.Builder builder) {
		return builder//
				.dataMimeType(MimeTypeUtils.TEXT_PLAIN)//
				.connectTcp(this.properties.getRsocket().getHostname(),
						this.properties.getRsocket().getPort())//
				.block();
	}

	private Client buildClient(RSocketRequester.Builder builder) {
		String clientId = UUID.randomUUID().toString();
		RSocketRequester rSocketRequester = this.rsocketRequester(builder);
		return new Client(rSocketRequester, clientId);
	}

}
