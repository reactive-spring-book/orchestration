package rsb.rsocket.errors.client;

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

@SpringBootApplication
public class ErrorApplication {

	@Bean
	RSocketRequester rSocketRequester(BootifulProperties properties,
			RSocketRequester.Builder builder) {
		return builder.connectTcp(properties.getRsocket().getHostname(),
				properties.getRsocket().getPort()).block();
	}

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(ErrorApplication.class, args);
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
		this.rSocketRequester.route("greetings").data("Spring Fans")
				.retrieveFlux(String.class).doOnError(log::error).subscribe(log::info);
	}

}
