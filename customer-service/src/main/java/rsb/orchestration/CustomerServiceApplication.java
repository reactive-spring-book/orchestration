package rsb.orchestration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SpringBootApplication
public class CustomerServiceApplication {

	public static void main(String args[]) {
		SpringApplication.run(CustomerServiceApplication.class, args);
	}

}

@RestController
class CustomerRestController {

	private final Map<Integer, Customer> customers = Map
			.of(1, "Jane", 2, "Mia", 3, "Leroy", 4, "Badhr", 5, "Zhen", 6, "Juliette", 7,
					"Artem", 8, "Michelle", 9, "Eva", 10, "Richard")//
			.entrySet()//
			.stream()//
			.collect(Collectors.toConcurrentMap(Map.Entry::getKey,
					e -> new Customer(e.getKey(), e.getValue())));

	private Flux<Customer> from(Stream<Customer> customerStream) {
		return Flux.fromStream(customerStream);
	}

	@GetMapping("/customers/{id}")
	Mono<Customer> byId(@PathVariable Integer id) {
		return Mono.just(this.customers.get(id));
	}

	@GetMapping("/customers")
	Flux<Customer> customers(@RequestParam(required = false) Integer[] ids) {
		var customerStream = this.customers.values().stream();
		return Optional//
				.ofNullable(ids)//
				.map(Arrays::asList)//
				.map(listOfIds -> from(customerStream.filter(customer -> {
					var id = customer.getId();
					return listOfIds.contains(id);
				})))//
				.orElse(from(customerStream));
	}

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Customer {

	private Integer id;

	private String name;

}