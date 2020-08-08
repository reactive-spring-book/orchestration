package rsb.rsocket.setup.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Log4j2
@Controller
class SetupController {

	@ConnectMapping("setup") // <1>
	public void setup(@Payload String setupPayload,
			@Headers Map<String, Object> headers) {// <2>
		log.info("setup payload: " + setupPayload);
		headers.forEach((k, v) -> log.info(k + '=' + v));
	}

}
