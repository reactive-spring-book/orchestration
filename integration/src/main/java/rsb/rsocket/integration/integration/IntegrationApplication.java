package rsb.rsocket.integration.integration;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.rsocket.RSocketInteractionModel;
import org.springframework.integration.rsocket.dsl.RSockets;
import org.springframework.messaging.MessageHeaders;
import rsb.rsocket.integration.GreetingResponse;

@Log4j2
@SpringBootApplication
public class IntegrationApplication {

	@Bean
    IntegrationFlow rsocketFlow() {
        var gateway = RSockets//
                .inboundGateway("greetings")//
                .
                .interactionModels(RSocketInteractionModel.requestStream);
        return IntegrationFlows
                .from(gateway)
                .handle(new GenericHandler<GreetingResponse>() {
                    @Override
                    public Object handle(GreetingResponse greetingResponse, MessageHeaders messageHeaders) {
                        log.info("new message :" + greetingResponse.getMessage() + ".");
                        messageHeaders.forEach((k, v) -> log.info(k + ':' + v));
                        return null;
                    }
                })
                .get();
    }

	public static void main(String[] a) {
		SpringApplication.run(IntegrationApplication.class, a);
	}

}
