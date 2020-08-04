package rsb.rsocket.encoding.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.rsocket.messaging.RSocketStrategiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import rsb.rsocket.encoding.GreetingRequest;
import rsb.rsocket.encoding.GreetingResponse;

import java.util.Map;

@SpringBootApplication
public class EncodingApplication {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "service");
		SpringApplication.run(EncodingApplication.class, args);
	}

	// we want to make sure we're the last bean to override the configuration
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	RSocketStrategiesCustomizer rSocketStrategiesCustomizer() {

		return strategies -> strategies.decoder(new Jackson2JsonDecoder())
				.encoder(new Jackson2JsonEncoder());
	}

}

@Configuration
class Config {

	public static void enumerate(
			ObjectProvider<RSocketStrategiesCustomizer> customizers) {
		customizers.orderedStream().forEach(rsc -> System.out.println(rsc.toString()));
	}

}

@Log4j2
@Controller
class GreetingController {

	@MessageMapping("greetings")
	Mono<GreetingResponse> greet(@Payload GreetingRequest request,
			@Headers Map<String, Object> headers) {
		headers.forEach((k, v) -> log.info(k + '=' + v));
		return Mono.just(new GreetingResponse("Hello, " + request.getName() + "!"));
	}

}

/*
 *
 * class GreetingXmlEncoder extends GreetingXmlSupport implements Encoder<Object> {
 *
 * @Override public boolean canEncode(ResolvableType resolvableType, MimeType mimeType) {
 * return GreetingResponse.class.isAssignableFrom(resolvableType.toClass()) &&
 * supportsMimeType(mimeType); }
 *
 * @Override public Flux<DataBuffer> encode( Publisher<?> publisher, DataBufferFactory
 * dataBufferFactory, ResolvableType resolvableType, MimeType mimeType, Map<String,
 * Object> headers) {
 *
 * Flux<DataBuffer> map = Mono .just(publisher) .map(p -> (GreetingResponse) p)
 * .map(GreetingResponse::getMessage) .map(msg -> "<greeting message=\"" + msg + "\"/>")
 * .map(xml -> from(dataBufferFactory, xml)) .flatMapMany(Mono::just); return map; }
 *
 * @SneakyThrows private DataBuffer from(DataBufferFactory dbf, String xml) { DataBuffer
 * dataBuffer = dbf.allocateBuffer(); OutputStream outputStream =
 * dataBuffer.asOutputStream(); Writer writer = new BufferedWriter(new
 * OutputStreamWriter(outputStream)); writer.write(xml); return dataBuffer; }
 *
 * @Override public List<MimeType> getEncodableMimeTypes() { return
 * List.of(this.applicationXml); } }
 *
 * class GreetingXmlDecoder extends GreetingXmlSupport implements
 * Decoder<GreetingResponse> {
 *
 * @Override public boolean canDecode(ResolvableType resolvableType, MimeType mimeType) {
 * return true; }
 *
 * @Override public Flux<GreetingResponse> decode(Publisher<DataBuffer> publisher,
 * ResolvableType resolvableType, MimeType mimeType, Map<String, Object> map) { return
 * null; }
 *
 * @Override public Mono<GreetingResponse> decodeToMono(Publisher<DataBuffer> publisher,
 * ResolvableType resolvableType, MimeType mimeType, Map<String, Object> map) { return
 * null; }
 *
 * @Override public List<MimeType> getDecodableMimeTypes() { return
 * List.of(this.applicationXml); } }
 *
 * abstract class GreetingXmlSupport {
 *
 * protected MediaType applicationXml = MediaType.APPLICATION_XML;
 *
 * protected boolean supportsMimeType(@Nullable MimeType mimeType) { return (mimeType ==
 * null || applicationXml.isCompatibleWith(mimeType)); } }
 */