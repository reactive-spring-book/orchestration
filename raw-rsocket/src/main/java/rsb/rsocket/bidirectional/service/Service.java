package rsb.rsocket.bidirectional.service;

import io.rsocket.*;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.rsocket.BootifulProperties;
import rsb.rsocket.EncodingUtils;
import rsb.rsocket.bidirectional.ClientHealthState;
import rsb.rsocket.bidirectional.Constants;
import rsb.rsocket.bidirectional.GreetingRequest;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static rsb.rsocket.bidirectional.ClientHealthState.STOPPED;

@Slf4j
@Component
class Service implements ApplicationListener<ApplicationReadyEvent>, Ordered,
		SocketAcceptor, Constants {

	private final Map<String, RSocket> clients = new ConcurrentHashMap<>();

	private final GreetingService greetingService;

	private final BootifulProperties properties;

	private final EncodingUtils encodingUtils;

	Service(EncodingUtils encodingUtils, BootifulProperties properties,
			GreetingService gs) {
		this.greetingService = gs;
		this.properties = properties;
		this.encodingUtils = encodingUtils;
	}

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

		var metadataUtf8 = setup.getMetadataUtf8();
		var metadata = this.encodingUtils.decodeMetadata(metadataUtf8);
		var uid = (String) metadata.get(UID);
		log.info("received a new client connection @ " + new Date().toString() + " for #"
				+ uid);
		this.clients.put(uid, clientRsocket);

		return Mono.just(new AbstractRSocket() {

			@Override
			public Flux<Payload> requestStream(Payload payload) {

				// this will only emit a value if there's a STOPPED event
				var clientHealthStateFlux = clientRsocket//
						.requestStream(DefaultPayload.create(new byte[0]))//
						.map(p -> encodingUtils.decode(p.getDataUtf8(),
								ClientHealthState.class))//
						.filter(chs -> chs.getState().equalsIgnoreCase(STOPPED));

				// this will emit an infinite stream of values but we want it to stop when
				// a suitable ClientHealthState value is emitted
				var replyPayloadFlux = greetingService//
						.greet(encodingUtils.decode(payload.getDataUtf8(),
								GreetingRequest.class));

				return replyPayloadFlux // this is where the magic happens! we return
						// GreetingResponses...
						.takeUntilOther(clientHealthStateFlux)// UNTIL the
						// clientHealthStateFlux
						// returns a value (the first and only STOPPED event)
						.map(encodingUtils::encode)//
						.map(DefaultPayload::create)//
						.doOnComplete(() -> {
							clients.remove(uid);
							var size = clients.size();
							log.info("removing #" + uid
									+ " from the collection. There are still " + size
									+ " client" + (size == 1 ? "" : "s")
									+ " still connected.");
						});
			}
		});
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

}
