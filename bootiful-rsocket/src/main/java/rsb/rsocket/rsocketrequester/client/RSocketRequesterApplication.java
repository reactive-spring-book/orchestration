package rsb.rsocket.rsocketrequester.client;

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
import org.springframework.util.Assert;
import rsb.rsocket.BootifulProperties;

@SpringBootApplication
@RequiredArgsConstructor
public class RSocketRequesterApplication {

	@SneakyThrows
	public static void main(String[] args) {
		SpringApplication.run(RSocketRequesterApplication.class, args);
		System.in.read();
	}

	@Bean
	RSocketRequester customRSocketRequester(BootifulProperties properties,
			RSocketRequester.Builder builder) {
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
		var rsocket = this.rSocketRequester.rsocket(); // much easier!
		var availability = rsocket.availability();
		Assert.isTrue(availability == 1.0,
				"the availability must be 1.0 in order to proceed!");
		log.info("the data mimeType is " + this.rSocketRequester.dataMimeType());
		log.info("the metadata mimeType is " + this.rSocketRequester.metadataMimeType());
		this.rSocketRequester//
				.route("greetings")//
				.data("Spring fans")//
				.retrieveMono(String.class)//
				.subscribe(log::info);
	}

}
