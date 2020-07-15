package rsb.rsocket.fireandforget.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Log4j2
@Controller
class GreetingController {

	@MessageMapping("greeting")
	void greetName(String name) {
		log.info("new command sent to update the name '" + name + "'.");
	}

}
