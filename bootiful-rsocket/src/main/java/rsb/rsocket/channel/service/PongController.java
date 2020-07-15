package rsb.rsocket.channel.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Log4j2
@Controller
class PongController {

	@MessageMapping("pong")
	public Flux<String> pong(@Payload Flux<String> ping) {
		return ping.map(PingPongApplication::reply).doOnNext(log::info);
	}

}
