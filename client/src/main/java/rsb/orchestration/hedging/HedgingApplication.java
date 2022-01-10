package rsb.orchestration.hedging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import rsb.orchestration.GreetingResponse;

@Slf4j
@SpringBootApplication
public class HedgingApplication {

	// <1>
	@Bean
	HedgingExchangeFilterFunction hedgingExchangeFilterFunction(@Value("${rsb.lb.max-nodes:3}") int maxNodes,
			ReactiveDiscoveryClient rdc) {
		return new HedgingExchangeFilterFunction(rdc, maxNodes);
	}

	// <2>
	@Bean
	WebClient client(WebClient.Builder builder, HedgingExchangeFilterFunction hedgingExchangeFilterFunction) {
		return builder.filter(hedgingExchangeFilterFunction).build();
	}

	// <3>
	@Bean
	ApplicationListener<ApplicationReadyEvent> hedgingApplicationListener(WebClient client) {
		return event -> client//
				.get()//
				.uri("http://slow-service/greetings")//
				.retrieve()//
				.bodyToFlux(GreetingResponse.class)//
				.doOnNext(gr -> log.info(gr.toString()))//
				.doOnError(ex -> log.info(ex.toString()))//
				.subscribe();
	}

	public static void main(String[] args) {
		SpringApplication.run(HedgingApplication.class, args);
	}

}
