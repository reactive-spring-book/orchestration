package rsb.orchestration.hedging.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.reactivestreams.Publisher;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import rsb.orchestration.hedging.HedgingUtils;

import java.net.URI;
import java.time.Duration;

@Log4j2
@RequiredArgsConstructor
class HedgingExchangeFilterFunction implements ExchangeFilterFunction {

	private final ReactiveDiscoveryClient reactiveDiscoveryClient;

	private final int timeoutInSeconds = 10;

	private final int maxNodes;

	@Override
	public Mono<ClientResponse> filter(ClientRequest clientRequest,
			ExchangeFunction exchangeFunction) {
		var requestUrl = clientRequest.url();
		var apiName = requestUrl.getHost();
		return this.reactiveDiscoveryClient //
				.getInstances(apiName) //
				.collectList()//
				.map(HedgingUtils::shuffle)//
				.flatMapMany(Flux::fromIterable)//
				.take(maxNodes)//
				.map(si -> HedgingUtils.buildUriFromServiceInstance(si, requestUrl)) //
				.map(uri -> invoke(uri, clientRequest, exchangeFunction)) //
				.collectList() //
				.flatMap(list -> Flux.first(list)
						.timeout(Duration.ofSeconds(timeoutInSeconds)).singleOrEmpty());
	}

	private static Mono<ClientResponse> invoke(URI uri, ClientRequest request,
			ExchangeFunction next) {
		var newRequest = ClientRequest//
				.create(request.method(), uri) //
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
