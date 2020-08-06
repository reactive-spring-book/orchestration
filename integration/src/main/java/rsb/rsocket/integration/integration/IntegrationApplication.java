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
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import rsb.rsocket.BootifulProperties;
import rsb.rsocket.integration.GreetingRequest;
import rsb.rsocket.integration.GreetingResponse;

@Log4j2
@SpringBootApplication
public class IntegrationApplication {

    /*FluxMessageChannel*/

    @Bean
    MessageChannel channel() {
        return MessageChannels.flux().get();
    }

    @Bean
    ClientRSocketConnector clientRSocketConnector(
            RSocketStrategies strategies,
            BootifulProperties properties) {
        ClientRSocketConnector clientRSocketConnector = new ClientRSocketConnector(properties.getRsocket().getHostname(), properties.getRsocket().getPort());
        clientRSocketConnector.setRSocketStrategies(strategies);
        return clientRSocketConnector;
    }

    @Bean
    IntegrationFlow greetingFlow(ClientRSocketConnector clientRSocketConnector) {
        var names = new String[]{"Mario", "Richard", "Michelle", "Natalie", "Madhura", "Violetta", "Yuxin", "Olga", "Rob", "Jane", "Arun", "Heinz", "Venkat"};
        var messageSource = new MessageSource<String>() {

            @Override
            public Message<String> receive() {
                var len = names.length;
                var rand = Math.random() * len;
                var index = (int) rand;
                Assert.state(index <= (names.length - 1), "the index is not within range");
                var name = names[index];
                return MessageBuilder.withPayload(name).build();
            }
        };
        return IntegrationFlows//
                .from(messageSource, poller -> poller.poller(pm -> pm.fixedRate(1000)))//
                .transform(String.class, GreetingRequest::new)//
                .handle(RSockets//
                        .outboundGateway("greetings")//
                        .interactionModel(RSocketInteractionModel.requestStream)//
                        .expectedResponseType(GreetingResponse.class)//
                        .clientRSocketConnector(clientRSocketConnector)//
                )
                .channel(this.channel())
                .get();
    }

    @Bean
    IntegrationFlow greetingsResponseFlow() {
        return IntegrationFlows
                .from(this.channel())
                .fluxTransform(messageFlux -> messageFlux.map(Message::getPayload))
                .handle(new GenericHandler<Flux<GreetingResponse>>() {
                    @Override
                    public Object handle(Flux<GreetingResponse> responses, MessageHeaders headers) {
                        responses.subscribe(gr -> log.info(gr.toString()));//todo there has to be someway to flatten this and handle the messages downstream using SI handlers?
                        return null;
                    }
                })
                .get();
    }

    public static void main(String[] a) {
        SpringApplication.run(IntegrationApplication.class, a);
    }

}
