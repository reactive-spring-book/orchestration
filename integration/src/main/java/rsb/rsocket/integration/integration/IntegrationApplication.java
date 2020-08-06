package rsb.rsocket.integration.integration;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.rsocket.ClientRSocketConnector;
import org.springframework.integration.rsocket.RSocketInteractionModel;
import org.springframework.integration.rsocket.dsl.RSockets;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.support.MessageBuilder;
import rsb.rsocket.BootifulProperties;
import rsb.rsocket.integration.GreetingRequest;
import rsb.rsocket.integration.GreetingResponse;

import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
@SpringBootApplication
public class IntegrationApplication {

	@Bean
	MessageChannel channel() {
		return MessageChannels.flux().get();
	}

	@Bean
	ClientRSocketConnector clientRSocketConnector(RSocketStrategies strategies,
			BootifulProperties properties) {
		ClientRSocketConnector clientRSocketConnector = new ClientRSocketConnector(
				properties.getRsocket().getHostname(), properties.getRsocket().getPort());
		clientRSocketConnector.setRSocketStrategies(strategies);
		return clientRSocketConnector;
	}

	@Bean
	MessageSource<String> arrayMessageSource() {
		var names = new String[] { "Mia", "Michelle", "Mario", "Richard", "Tammie",
				"Kimly", "Natalie", "Madhura", "Violetta", "Yuxin", "Olga", "Rob", "Jane",
				"Artem", "Gary", "Mark", "Oleg", "Arun", "Heinz", "Venkat" };
		var ctr = new AtomicInteger();
		return () -> {
			var indx = ctr.getAndIncrement();
			return (indx < names.length) ? MessageBuilder.withPayload(names[indx]).build()
					: null;
		};
	}

	@Bean
	IntegrationFlow greetingFlow(ClientRSocketConnector clientRSocketConnector) {
		return IntegrationFlows//
				.from(arrayMessageSource(),
						poller -> poller.poller(pm -> pm.fixedRate(100)))//
				.transform(String.class, GreetingRequest::new)//
				.handle(RSockets//
						.outboundGateway("greetings")//
						.interactionModel(RSocketInteractionModel.requestStream)//
						.expectedResponseType(GreetingResponse.class)//
						.clientRSocketConnector(clientRSocketConnector)//
				)//
				.split()//
				.channel(this.channel())
				.handle((GenericHandler<GreetingResponse>) (payload, headers) -> {
					log.info("-----------------");
					log.info(payload.toString());
					headers.forEach((header, value) -> log.info(header + "=" + value));
					return null;
				})//
				.get();
	}

	public static void main(String[] a) {
		SpringApplication.run(IntegrationApplication.class, a);
	}

}
