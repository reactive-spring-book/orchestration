package rsb.rsocket.metadata.client;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.metadata.CompositeMetadataFlyweight;
import io.rsocket.util.ByteBufPayload;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.rsocket.metadata.Constants;

import java.time.Duration;
import java.util.Locale;

@Log4j2
class Client implements ApplicationListener<ApplicationReadyEvent> {

	private final RSocketRequester rSocketRequester;

	private final String clientId;

	Client(RSocketRequester rSocketRequester, String clientId) {
		this.rSocketRequester = rSocketRequester;
		this.clientId = clientId;
	}

	private void encodeMetadataHeader(CompositeByteBuf metadataByteBuf,
			String metadataHeaderContentType, byte[] bytes) {
		CompositeMetadataFlyweight.encodeAndAddMetadata(metadataByteBuf,
				ByteBufAllocator.DEFAULT, metadataHeaderContentType,
				ByteBufAllocator.DEFAULT.buffer().writeBytes(bytes));
	}

	private Mono<Void> update(Locale locale) {
		CompositeByteBuf metadataByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
		encodeMetadataHeader(metadataByteBuf, Constants.LANG_VALUE,
				locale.getLanguage().getBytes());
		encodeMetadataHeader(metadataByteBuf, Constants.CLIENT_ID_VALUE,
				this.clientId.getBytes());
		return this.rSocketRequester//
				.rsocket()//
				.metadataPush(
						ByteBufPayload.create(Unpooled.EMPTY_BUFFER, metadataByteBuf));
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		Flux//
				.just(Locale.CHINA, Locale.FRANCE, Locale.JAPANESE)//
				.delayElements(Duration.ofSeconds((long) (Math.random() * 30)))
				.flatMap(this::update)//
				.subscribe();//
	}

}
