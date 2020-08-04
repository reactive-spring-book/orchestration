package rsb.rsocket.bidirectional.client;

import io.rsocket.*;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.Lifecycle;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.rsocket.EncodingUtils;
import rsb.rsocket.bidirectional.ClientHealthState;
import rsb.rsocket.bidirectional.Constants;
import rsb.rsocket.bidirectional.GreetingRequest;
import rsb.rsocket.bidirectional.GreetingResponse;

import java.time.Duration;
import java.util.Date;
import java.util.stream.Stream;

import static rsb.rsocket.bidirectional.ClientHealthState.STARTED;
import static rsb.rsocket.bidirectional.ClientHealthState.STOPPED;

@Slf4j
class Client implements SocketAcceptor, Constants/* , Lifecycle */ {

	private final EncodingUtils encodingUtils;

	private final String uid;

	private final String serviceHostname;

	private final int servicePort;

	// <1>
	Client(EncodingUtils utils, String uuid, String svcHost, int svcPort) {
		this.uid = uuid;
		this.encodingUtils = utils;
		this.serviceHostname = svcHost;
		this.servicePort = svcPort;
	}

	Flux<GreetingResponse> start() {
		log.info("launching " + this.uid + " @ " + new Date().toString());
		var greetingRequestPayload = this.encodingUtils
				.encode(new GreetingRequest("Client #" + this.uid));
		return RSocketFactory//
				.connect()//
				.acceptor(this)//
				.transport(
						TcpClientTransport.create(this.serviceHostname, this.servicePort)) //
				.start()//
				.flatMapMany(instance -> instance // <2>
						.requestStream(DefaultPayload.create(greetingRequestPayload)) //
						.map(payload -> encodingUtils.decode(payload.getDataUtf8(),
								GreetingResponse.class))//
		);
	}

	// <3>
	@Override
	public Mono<RSocket> accept(ConnectionSetupPayload setup, RSocket serverRSocket) {

		return Mono.just(new AbstractRSocket() {

			@Override
			public Flux<Payload> requestStream(Payload payload) {

				var start = new Date().getTime();

				var delayInSeconds = ((long) (Math.random() * 30)) * 1000;

				var stateFlux = Flux.fromStream(Stream.generate(() -> {
					var now = new Date().getTime();
					var stop = ((start + delayInSeconds) < now) && Math.random() > .8;
					return new ClientHealthState(stop ? STOPPED : STARTED);
				}))//
						.delayElements(Duration.ofSeconds(5));

				return stateFlux//
						.map(encodingUtils::encode)//
						.map(DefaultPayload::create);
			}
		});
	}

}
