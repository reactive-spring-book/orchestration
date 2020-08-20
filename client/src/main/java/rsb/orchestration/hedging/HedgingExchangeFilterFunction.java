package rsb.orchestration.hedging;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;

@Log4j2
@RequiredArgsConstructor
class HedgingExchangeFilterFunction implements ExchangeFilterFunction {

	private final ReactiveLoadBalancer.Factory<ServiceInstance> serviceInstanceFactory;

	private final int timeoutInSeconds = 10;

	private final int maxNodes;

	@Override
	public Mono<ClientResponse> filter(ClientRequest clientRequest,
			ExchangeFunction exchangeFunction) {
		var requestUrl = clientRequest.url();
		var apiName = requestUrl.getHost();
		var api = serviceInstanceFactory.getInstance(apiName);
		var responses = new ArrayList<Mono<ClientResponse>>();
		while (responses.size() < maxNodes) {
			var clientResponse = Mono//
					.from(api.choose())//
					.map(responseServiceInstance -> {
						var server = responseServiceInstance.getServer();
						var key = (requestUrl.getScheme() + "://" + server.getHost() + ':'
								+ server.getPort() + requestUrl.getPath());
						log.debug("the key is  " + key);
						return key;
					})//
					.flatMap(uri -> invoke(uri, clientRequest, exchangeFunction))
					.timeout(Duration.ofSeconds(timeoutInSeconds));

			responses.add(clientResponse);
		}
		log.info("there are " + responses.size()
				+ " elements in the responses collection");

		Flux<ClientResponse> clientResponseFlux = Flux.fromIterable(responses)
				.flatMap(x -> x);

		return Flux.first(clientResponseFlux).take(1).singleOrEmpty();
	}

	private Mono<ClientResponse> invoke(String uri, ClientRequest request,
			ExchangeFunction next) {
		var newRequest = ClientRequest//
				.create(request.method(), URI.create(uri))//
				.headers(h -> h.addAll(request.headers()))//
				.cookies(c -> c.addAll(request.cookies()))//
				.attributes(a -> a.putAll(request.attributes()))//
				.body(request.body())//
				.build();
		return next//
				.exchange(newRequest)//
				.doOnNext(cr -> log.info("launching " + newRequest.url()));
	}

}
