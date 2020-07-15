package rsb.rsocket.metadata.service;

import io.rsocket.*;
import io.rsocket.transport.netty.server.TcpServerTransport;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import rsb.rsocket.BootifulProperties;
import rsb.rsocket.EncodingUtils;
import rsb.rsocket.metadata.Constants;

import java.util.Map;

@Log4j2
@Component
@RequiredArgsConstructor
class Service implements SocketAcceptor, ApplicationListener<ApplicationReadyEvent> {

	private final EncodingUtils encodingUtils;

	private final BootifulProperties properties;

	private static void log(StringBuilder stringBuilder, String clientId, String string) {
		stringBuilder//
				.append(String.format("(%s) %s", clientId, string))//
				.append(System.lineSeparator());
	}

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

	private Mono<Void> onMetadataPush(Map<String, Object> metadata) {
		var clientId = (String) metadata.get(Constants.CLIENT_ID_HEADER);
		var stringBuilder = new StringBuilder().append(System.lineSeparator());
		log(stringBuilder, clientId, "---------------------------------");
		metadata.forEach((k, v) -> log(stringBuilder, clientId, k + '=' + v));
		log.info(stringBuilder.toString());
		return Mono.empty();
	}

	//
	@Override
	public Mono<RSocket> accept(ConnectionSetupPayload connectionSetupPayload,
			RSocket rSocket) {
		var rs = new AbstractRSocket() {

			@Override
			public Mono<Void> metadataPush(Payload payload) {
				var metadataUtf8 = payload.getMetadataUtf8();
				onMetadataPush(encodingUtils.decodeMetadata(metadataUtf8));
				return Mono.empty();
			}
		};
		return Mono.just(rs);
	}

}
