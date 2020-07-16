package rsb.rsocket.encoding.client;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import rsb.rsocket.BootifulProperties;
import rsb.rsocket.bidirectional.GreetingRequest;
import rsb.rsocket.encoding.GreetingResponse;

@SpringBootApplication
public class EncodingApplication {

	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE) // make sure we are guaranteed to be the last in
										// the line
	RSocketStrategiesCustomizer rSocketStrategiesCustomizer() {
		return strategies -> strategies.decoder(new Jackson2JsonDecoder())
				.encoder(new Jackson2JsonEncoder());
	}

	@Bean
	RSocketRequester rSocketRequester(BootifulProperties properties,
			RSocketRequester.Builder builder) {
		return builder//
				.connectTcp(properties.getRsocket().getHostname(),
						properties.getRsocket().getPort()) //
				.block();
	}

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(EncodingApplication.class, args);
		System.in.read();
	}

}

@Log4j2
@Component
@RequiredArgsConstructor
class Client implements ApplicationListener<ApplicationReadyEvent> {

	private final RSocketRequester rSocketRequester;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		this.rSocketRequester.route("greetings").data(new GreetingRequest("Spring fans"))
				.retrieveMono(GreetingResponse.class).subscribe(log::info);
	}

}
