package rsb.orchestration.hedging;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import rsb.orchestration.Customer;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Make sure to start `eureka-service` and at least three instances of `customer-service`
 * <p>
 * TODO: introduce some delay in the `customer-service` to simulate the benefits of
 * hedging
 */
@Log4j2
@SpringBootApplication
public class HedgingApplication {

	@Bean
	ApplicationListener<ApplicationReadyEvent> hedge(WebClient client) {
		return event -> client//
				.get()//
				.uri("http://customer-service/customers")//
				.retrieve()//
				.bodyToFlux(Customer.class)//
				.subscribe(log::info);
	}

	@Bean
	HedgingExchangeFilterFunction hedgingExchangeFilterFunction(
			@Value("${rsb.lb.max-nodes:2}") int maxNodes,
			ReactiveLoadBalancer.Factory<ServiceInstance> rlb) {
		return new HedgingExchangeFilterFunction(rlb, maxNodes);
	}

	@Bean
	WebClient client(WebClient.Builder builder,
			HedgingExchangeFilterFunction hedgingExchangeFilterFunction) {
		return builder.filter(hedgingExchangeFilterFunction).build();
	}

	public static void main(String[] args) {
		SpringApplication.run(HedgingApplication.class, args);
	}

}

@Log4j2
@RequiredArgsConstructor
class HedgingExchangeFilterFunction implements ExchangeFilterFunction {

	private final ReactiveLoadBalancer.Factory<ServiceInstance> serviceInstanceFactory;

	private final int maxNodes;

	private Flux<ClientResponse> callDistinctNodes(int maxNodes, ClientRequest request,
			ReactiveLoadBalancer.Factory<ServiceInstance> serviceInstanceFactory,
			ExchangeFunction next) {
		var requestUrl = request.url();
		var apiName = requestUrl.getHost();
		var api = serviceInstanceFactory.getInstance(apiName);
		var chosen = Flux.from(api.choose());
		return chosen//
				.map(responseServiceInstance -> {
					var server = responseServiceInstance.getServer();
					return Tuples.of(
							server.getScheme() + "://" + server.getHost() + ':'
									+ server.getPort(),
							URI.create(requestUrl.getScheme() + "://" + server.getHost()
									+ ':' + server.getPort() + "/"
									+ requestUrl.getPath()));
				})//
				.distinct((Function<Tuple2<String, URI>, Object>) Tuple2::getT1)//
				.flatMap(tuple -> invoke(tuple.getT2(), request, next))//
				.take(maxNodes);
	}

	private Mono<ClientResponse> invoke(URI uri, ClientRequest request,
			ExchangeFunction next) {
		var newRequest = ClientRequest//
				.create(request.method(), uri)//
				.headers(h -> h.addAll(request.headers()))//
				.cookies(c -> c.addAll(request.cookies()))//
				.attributes(a -> a.putAll(request.attributes()))//
				.body(request.body())//
				.build();
		return next.exchange(newRequest)//
				.doOnNext(cr -> log.info("launching " + newRequest.url()));
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest clientRequest,
			ExchangeFunction exchangeFunction) {
		var clientResponseFlux = callDistinctNodes(this.maxNodes, clientRequest,
				serviceInstanceFactory, exchangeFunction);
		return Flux.first(clientResponseFlux).singleOrEmpty();
	}

}

@Deprecated
@Log4j2
class ClassicHedgeExchangeFilterFunction implements ExchangeFilterFunction {

	private final DiscoveryClient discoveryClient;

	private final LoadBalancerClient loadBalancerClient;

	private final int attempts, maxAttempts;

	ClassicHedgeExchangeFilterFunction(DiscoveryClient discoveryClient,
			LoadBalancerClient loadBalancerClient, int attempts) {
		this.discoveryClient = discoveryClient;
		this.loadBalancerClient = loadBalancerClient;
		this.attempts = attempts;
		this.maxAttempts = attempts * 2;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		URI originalURI = request.url();
		String serviceId = originalURI.getHost();
		List<ServiceInstance> serviceInstanceList = this.discoveryClient
				.getInstances(serviceId);
		Assert.state(serviceInstanceList.size() >= this.attempts,
				() -> "there must be at least " + this.attempts
						+ " instances of the service " + serviceId + "!");
		int counter = 0;
		Map<String, Mono<ClientResponse>> ships = new HashMap<>();
		while (ships.size() < this.attempts && (counter++ < this.maxAttempts)) {
			ServiceInstance lb = this.loadBalancerClient.choose(serviceId);
			String asciiString = lb.getUri().toASCIIString();
			ships.computeIfAbsent(asciiString,
					str -> this.invoke(lb, originalURI, request, next));
		}
		return Flux.first(ships.values()).singleOrEmpty();
	}

	private Mono<ClientResponse> invoke(ServiceInstance serviceInstance, URI originalURI,
			ClientRequest request, ExchangeFunction next) {
		URI uri = this.loadBalancerClient.reconstructURI(serviceInstance, originalURI);
		ClientRequest newRequest = ClientRequest.create(request.method(), uri)
				.headers(h -> h.addAll(request.headers()))
				.cookies(c -> c.addAll(request.cookies()))
				.attributes(a -> a.putAll(request.attributes())).body(request.body())
				.build();
		return next.exchange(newRequest)
				.doOnNext(cr -> log.info("launching " + newRequest.url()));
	}

}
