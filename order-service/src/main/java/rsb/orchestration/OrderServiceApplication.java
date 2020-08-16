package rsb.orchestration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SpringBootApplication
public class OrderServiceApplication {

	public static void main(String args[]) {
		SpringApplication.run(OrderServiceApplication.class, args);
	}

}

@RequestMapping("/orders")
@RestController
class OrderRestController {

	// customerId -> orders
	private final Map<Integer, List<Order>> orders = //
			IntStream//
					.range(0, 10)//
					.boxed()//
					.map(id -> Map.entry(id, new CopyOnWriteArrayList<Order>()))
					.collect(Collectors.toConcurrentMap(Map.Entry::getKey, e -> {
						var listOfOrders = e.getValue();
						var max = (int) (Math.random() * 10);
						if (max < 1) {
							max = 1;
						}
						for (var i = 0; i < max; i++) {
							listOfOrders.add(
									new Order(UUID.randomUUID().toString(), e.getKey()));
						}
						return listOfOrders;
					}));

	@GetMapping("/{id}")
	Mono<Collection<Order>> byId(@PathVariable Integer id) {
		return Mono.just(this.orders.get(id));
	}

	@GetMapping
	Mono<Map<Integer, List<Order>>> orders(
			@RequestParam(required = false) Integer[] ids) {
		var customerStream = this.orders.keySet().stream();
		var includedCustomerIds = Arrays.asList(ids);
		var ordersForCustomer = customerStream.filter(includedCustomerIds::contains)
				.map(id -> Map.entry(id, this.orders.get(id))).collect(Collectors
						.toConcurrentMap(Map.Entry::getKey, Map.Entry::getValue));
		return Mono.just(ordersForCustomer);
	}

}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Order {

	private String id;

	private Integer customerId;

}