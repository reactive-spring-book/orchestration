package rsb.rsocket.requestresponse.service;

import io.rsocket.*;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import rsb.rsocket.BootifulProperties;

@Log4j2
@Component
@RequiredArgsConstructor
class Service
		implements SocketAcceptor, Ordered, ApplicationListener<ApplicationReadyEvent> {

	private final BootifulProperties properties;

	// <1>
	@Override
	public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
		log.info("starting " + Service.class.getName() + '.');
		RSocketFactory //
				.receive()// <2>
				.acceptor(this)// <3>
				.transport(TcpServerTransport.create(// <4>
						this.properties.getRsocket().getHostname(),
						this.properties.getRsocket().getPort()))//
				.start() // <5>
				.subscribe();//
	}

	@Override // <6>
	public Mono<RSocket> accept(ConnectionSetupPayload connectionSetupPayload,
			RSocket rSocket) {
		var rs = new AbstractRSocket() {// <7>

			@Override
			public Mono<Payload> requestResponse(Payload payload) {
				return Mono
						.just(DefaultPayload.create("Hello, " + payload.getDataUtf8()));
			}
		};

		return Mono.just(rs);
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
