package rsb.rsocket.metadata.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import rsb.rsocket.metadata.Constants;

import java.util.Locale;
import java.util.UUID;

@Log4j2
@Component
@RequiredArgsConstructor
class Client implements ApplicationListener<ApplicationReadyEvent> {

	private final RSocketRequester rSocketRequester;

	private final String clientId = UUID.randomUUID().toString();

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		this.rSocketRequester.route("metadata")
				.metadata(this.clientId, Constants.CLIENT_ID)
				.metadata(Locale.JAPANESE.getLanguage(), Constants.LANG)
				.data(Mono.empty()).send().subscribe();
	}

}

/*
 *
 *
 *
 * // Client(RSocketRequester rSocketRequester, String clientId) { //
 * this.rSocketRequester = rSocketRequester; //// this.clientId = clientId; // }
 *
 * private void encodeMetadataHeader( CompositeByteBuf metadataByteBuf, MimeType
 * headerContentType, byte[] bytes) { CompositeMetadataFlyweight//
 * .encodeAndAddMetadata(metadataByteBuf, ByteBufAllocator.DEFAULT,
 * headerContentType.toString(), ByteBufAllocator.DEFAULT.buffer().writeBytes(bytes)); }
 *
 * private Mono<Void> sendMetadataLocaleUpdate(Locale locale) { CompositeByteBuf
 * metadataByteBuf = buildMetadataCompositeByteBuf(locale); return this.rSocketRequester//
 * .rsocket()// .metadataPush( ByteBufPayload.create(Unpooled.EMPTY_BUFFER,
 * metadataByteBuf)); }
 *
 * private CompositeByteBuf buildMetadataCompositeByteBuf(Locale locale) { Map<MimeType,
 * byte[]> headers = Map.of( Constants.LANG, locale.getLanguage().getBytes(), //
 * Constants.CLIENT_ID, this.clientId.getBytes() ); CompositeByteBuf metadataByteBuf =
 * ByteBufAllocator.DEFAULT.compositeBuffer(); headers.forEach((mimeType, value) ->
 * encodeMetadataHeader(metadataByteBuf, mimeType, value)); return metadataByteBuf; }
 *
 * private void originalClient() { Flux// .just(Locale.CHINA, Locale.FRANCE,
 * Locale.JAPANESE)// .delayElements(Duration.ofSeconds((long) (Math.random() * 30)))
 * .flatMap(this::sendMetadataLocaleUpdate)// .subscribe(); }
 */