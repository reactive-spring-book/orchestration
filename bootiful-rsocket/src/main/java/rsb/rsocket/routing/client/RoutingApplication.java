package rsb.rsocket.routing.client;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import rsb.rsocket.BootifulProperties;
import rsb.rsocket.routing.Customer;

@SpringBootApplication
public class RoutingApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(RoutingApplication.class, args);
		System.in.read();
	}

	@Bean
	RSocketRequester rSocketRequester(RSocketRequester.Builder builder,
			BootifulProperties properties) {
		return builder.connectTcp(properties.getRsocket().getHostname(),
				properties.getRsocket().getPort()).block();
	}

}

@Log4j2
@Component
@RequiredArgsConstructor
class Client implements ApplicationListener<ApplicationReadyEvent> {

	private final RSocketRequester rSocketRequester;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		var id = 1;
		this.rSocketRequester.route("customers.{id}", id).retrieveMono(Customer.class)
				.subscribe(log::info);
	}

}
