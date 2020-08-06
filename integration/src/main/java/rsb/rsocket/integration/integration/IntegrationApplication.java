package rsb.rsocket.integration.integration;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.integration.rsocket.ClientRSocketConnector;
import org.springframework.integration.rsocket.RSocketInteractionModel;
import org.springframework.integration.rsocket.dsl.RSockets;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.rsocket.RSocketStrategies;
import rsb.rsocket.BootifulProperties;
import rsb.rsocket.integration.GreetingResponse;

@Log4j2
@SpringBootApplication
public class IntegrationApplication {

    @Bean
    RSocketStrategiesCustomizer customizer() {
        return strategies -> strategies//
                .encoder(new Jackson2JsonEncoder())//
                .decoder(new Jackson2JsonDecoder())//
                .build();
    }

    @Bean
    ClientRSocketConnector clientRSocketConnector(
            RSocketStrategies strategies,
            BootifulProperties properties) {
        ClientRSocketConnector clientRSocketConnector =
                new ClientRSocketConnector(properties.getRsocket().getHostname(), properties.getRsocket().getPort());
        clientRSocketConnector.setRSocketStrategies(strategies);
//        clientRSocketConnector.setSetupRoute("clientConnect/{user}");
//        clientRSocketConnector.setSetupRouteVariables("myUser");
        return clientRSocketConnector;
    }


    @Bean
    IntegrationFlow rsocketFlow() {
        var gateway = RSockets//
                .inboundGateway("greetings")//
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
