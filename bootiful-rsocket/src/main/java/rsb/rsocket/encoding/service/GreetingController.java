package rsb.rsocket.encoding.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import rsb.rsocket.encoding.GreetingRequest;
import rsb.rsocket.encoding.GreetingResponse;

import java.util.Map;

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
