package rsb.rsocket.integration.integration;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.transformer.FileToStringTransformer;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.rsocket.ClientRSocketConnector;
import org.springframework.integration.rsocket.RSocketInteractionModel;
import org.springframework.integration.rsocket.dsl.RSockets;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.rsocket.RSocketStrategies;
import rsb.rsocket.BootifulProperties;
import rsb.rsocket.integration.GreetingRequest;
import rsb.rsocket.integration.GreetingResponse;

import java.io.File;

@Log4j2
@SpringBootApplication
public class IntegrationApplication {

	@Bean
	ClientRSocketConnector clientRSocketConnector(RSocketStrategies strategies,
			BootifulProperties properties) {// <1>
		ClientRSocketConnector clientRSocketConnector = new ClientRSocketConnector(
				properties.getRsocket().getHostname(), properties.getRsocket().getPort());
		clientRSocketConnector.setRSocketStrategies(strategies);
		return clientRSocketConnector;
	}

	@Bean
	IntegrationFlow greetingFlow(@Value("${user.home}") File home,
			ClientRSocketConnector clientRSocketConnector) {

		var inboundFileAdapter = Files// <2>
				.inboundAdapter(new File(home, "in"))//
				.autoCreateDirectory(true);

		return IntegrationFlows//
				.from(inboundFileAdapter,
						poller -> poller.poller(pm -> pm.fixedRate(100)))// <3>
				.transform(new FileToStringTransformer())// <4>
				.transform(String.class, GreetingRequest::new)// <5>
				.handle(RSockets//
						.outboundGateway("greetings")//
						.interactionModel(RSocketInteractionModel.requestStream)//
						.expectedResponseType(GreetingResponse.class)//
						.clientRSocketConnector(clientRSocketConnector)//
				)//
				.split()// <7>
				.channel(this.channel()) // <8>
				.handle((GenericHandler<GreetingResponse>) (payload, headers) -> {// <9>
					log.info("-----------------");
					log.info(payload.toString());
					headers.forEach((header, value) -> log.info(header + "=" + value));
					return null;
				})//
				.get();
	}

	@Bean
	MessageChannel channel() {
		return MessageChannels.flux().get();// <10>
	}

	public static void main(String[] args) {
		SpringApplication.run(IntegrationApplication.class, args);
	}

}
