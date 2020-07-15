package rsb.rsocket.routing.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.rsocket.routing.Customer;

import java.util.Map;

@SpringBootApplication
public class RoutingApplication {

	public static void main(String[] args) {
		System.setProperty("spring.profiles.active", "service");
		SpringApplication.run(RoutingApplication.class, args);
	}

}

@Controller
class RoutingController {

	private final Map<Integer, Customer> customers = Map.of(1, new Customer(1, "Zhen"), 2,
			new Customer(2, "Zhouyue"));

	@MessageMapping("customers")
	Flux<Customer> all() {
		return Flux.fromStream(this.customers.values().stream());
	}

	@MessageMapping("customers.{id}")
	Mono<Customer> byId(@DestinationVariable Integer id) {
		return Mono.justOrEmpty(this.customers.get(id));
	}

}
