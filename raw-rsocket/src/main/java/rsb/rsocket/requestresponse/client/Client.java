package rsb.rsocket.requestresponse.client;

import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import rsb.rsocket.BootifulProperties;

@Log4j2
@Component
@RequiredArgsConstructor
class Client implements ApplicationListener<ApplicationReadyEvent>, Ordered {

	private final BootifulProperties properties;

	@Override
	public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
		log.info("starting " + Client.class.getName() + '.');
		Flux<String> reply = RSocketFactory//
				.connect()// <1>
				.transport(TcpClientTransport.create(
						this.properties.getRsocket().getHostname(),
						this.properties.getRsocket().getPort()))//
				.start()//
				.flatMapMany(socket -> { // <2>
					var reactiveSpring = DefaultPayload.create("Reactive Spring");
					return socket//
							.requestResponse(reactiveSpring)//
							.map(Payload::getDataUtf8);
				});
		reply.subscribe(log::info);
	}

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

}
