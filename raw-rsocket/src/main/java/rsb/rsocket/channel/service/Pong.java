package rsb.rsocket.channel.service;

import io.rsocket.*;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.rsocket.BootifulProperties;

@Log4j2
@Component
@RequiredArgsConstructor
class Pong implements SocketAcceptor, ApplicationListener<ApplicationReadyEvent> {

	private final BootifulProperties properties;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {

		RSocketFactory //
				.receive()//
				.acceptor(this)//
				.transport(TcpServerTransport.create(
						this.properties.getRsocket().getHostname(),
						this.properties.getRsocket().getPort()))
				.start() //
				.subscribe();
	}

	@Override
	public Mono<RSocket> accept(ConnectionSetupPayload connectionSetupPayload,
			RSocket rSocket) {

		var rs = new AbstractRSocket() {

			@Override
			public Flux<Payload> requestChannel(Publisher<Payload> payloads) {
				return Flux //
						.from(payloads) //
						.map(Payload::getDataUtf8) //
						.doOnNext(
								str -> log.info("received " + str + "  in " + getClass())) //
						.map(PingPongApplication::reply) //
						.map(DefaultPayload::create);
			}
		};

		return Mono.just(rs);
	}

}
