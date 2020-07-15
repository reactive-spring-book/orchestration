package rsb.rsocket.requestresponse.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.Map;

@Log4j2
@Controller
class GreetingController {

	@MessageMapping("greeting")
	Mono<String> greet(@Headers Map<String, Object> headers, @Payload String name) {
		headers.forEach((k, v) -> log.info(k + '=' + v));
		return Mono.just("Hello, " + name + "!");
	}

}
