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
import rsb.rsocket.EncodingUtils;

@Log4j2
@Component
@RequiredArgsConstructor
class Service
		implements SocketAcceptor, Ordered, ApplicationListener<ApplicationReadyEvent> {

	private final EncodingUtils encodingUtils;

	private final BootifulProperties properties;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
		log.info("starting " + Service.class.getName() + '.');
		RSocketFactory //
				.receive()//
				.acceptor(this)//
				.transport(TcpServerTransport.create(
						this.properties.getRsocket().getHostname(),
						this.properties.getRsocket().getPort()))//
				.start() //
				.subscribe();
	}

	@Override
	public Mono<RSocket> accept(ConnectionSetupPayload connectionSetupPayload,
			RSocket rSocket) {
		var rs = new AbstractRSocket() {

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
