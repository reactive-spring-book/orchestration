package rsb.rsocket.metadata.client;

import io.rsocket.Payload;
import io.rsocket.RSocketFactory;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import reactor.core.publisher.Flux;
import rsb.rsocket.BootifulProperties;
import rsb.rsocket.EncodingUtils;
import rsb.rsocket.metadata.Constants;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;

@Log4j2
class Client implements ApplicationListener<ApplicationReadyEvent> {

	private final EncodingUtils encodingUtils;

	private final String clientId;

	private final BootifulProperties properties;

	Client(BootifulProperties properties, EncodingUtils encodingUtils, String clientId) {
		this.encodingUtils = encodingUtils;
		this.clientId = clientId;
		this.properties = properties;
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		RSocketFactory//
				.connect()//
				.transport(TcpClientTransport.create(
						this.properties.getRsocket().getHostname(),
						this.properties.getRsocket().getPort()))//
				.start()//
				.flatMapMany(rSocket -> Flux//
						.just(Locale.CHINA, Locale.FRANCE, Locale.JAPANESE)//
						.delayElements(Duration.ofSeconds((long) (Math.random() * 30)))
						.flatMap(locale -> rSocket
								.metadataPush(update(this.clientId, locale)))//
				) //
				.subscribe();
	}

	private Payload update(String clientId, Locale locale) {
		var map = Map.<String, Object>of(Constants.LANG_HEADER, locale.getLanguage(),
				Constants.CLIENT_ID_HEADER, clientId);
		return DefaultPayload.create("", encodingUtils.encodeMetadata(map));
	}

}
