package rsb.rsocket.bidirectional.service;

import io.rsocket.*;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.rsocket.BootifulProperties;
import rsb.rsocket.EncodingUtils;
import rsb.rsocket.bidirectional.ClientHealthState;
import rsb.rsocket.bidirectional.GreetingRequest;
import rsb.rsocket.bidirectional.GreetingResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Stream;

import static rsb.rsocket.bidirectional.ClientHealthState.STOPPED;

@Slf4j
@Component
@RequiredArgsConstructor
class Service implements ApplicationListener<ApplicationReadyEvent>, SocketAcceptor {

	private final BootifulProperties properties;

	private final EncodingUtils encodingUtils;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent are) {
		log.info("starting " + this.getClass().getName());
		RSocketFactory//
				.receive()//
				.acceptor(this)//
				.transport(TcpServerTransport.create(
						this.properties.getRsocket().getHostname(),
						this.properties.getRsocket().getPort()))//
				.start()//
				.subscribe();
	}

	@Override
	public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket clientRsocket) {

		// <1>
		return Mono.just(new AbstractRSocket() {

			@Override
			public Flux<Payload> requestStream(Payload payload) {

				// <2>
				var clientHealthStateFlux = clientRsocket//
						.requestStream(DefaultPayload.create(new byte[0]))//
						.map(p -> encodingUtils.decode(p.getDataUtf8(),
								ClientHealthState.class))//
						.filter(chs -> chs.getState().equalsIgnoreCase(STOPPED));

				// <3>
				var replyPayloadFlux = Flux//
						.fromStream(Stream.generate(() -> {
							var greetingRequest = encodingUtils
									.decode(payload.getDataUtf8(), GreetingRequest.class);
							var message = "Hello, " + greetingRequest.getName() + " @ "
									+ Instant.now() + "!";
							return new GreetingResponse(message);
						}))//
						.delayElements(Duration
								.ofSeconds(Math.max(3, (long) (Math.random() * 10))))//
						.doFinally(signalType -> log.info("finished."));

				return replyPayloadFlux // <4>
						.takeUntilOther(clientHealthStateFlux)//
						.map(encodingUtils::encode)//
						.map(DefaultPayload::create);
			}
		});
	}

}
