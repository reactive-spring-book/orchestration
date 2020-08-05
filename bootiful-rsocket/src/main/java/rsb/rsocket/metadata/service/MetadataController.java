package rsb.rsocket.metadata.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.Map;

@Log4j2
@Controller
@RequiredArgsConstructor
class MetadataController {

	@ConnectMapping
	Mono<Void> setup(@Headers Map<String, Object> metadata) {
		log.info("## setup");
		return enumerate(metadata);
	}

	@MessageMapping("metadata")
	Mono<Void> message(@Headers Map<String, Object> metadata) {
		log.info("## message");
		return enumerate(metadata);
	}

	private Mono<Void> enumerate(Map<String, Object> headers) {
		headers.forEach((header, value) -> log.info(header + ':' + value));
		return Mono.empty();
	}

}
