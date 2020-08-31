package rsb.orchestration.hedging;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
				.getInstances(apiName) // <1>
				.collectList()// <2>
				.map(HedgingExchangeFilterFunction::shuffle)// <3>
				.flatMapMany(Flux::fromIterable)// <4>
				.take(maxNodes)// <5>
				.map(si -> buildUriFromServiceInstance(si, requestUrl)) // <6>
				.map(uri -> invoke(uri, clientRequest, exchangeFunction)) // <7>
				.collectList() // <8>
				.flatMap(list -> Flux.first(list)
						.timeout(Duration.ofSeconds(timeoutInSeconds)).singleOrEmpty());// <9>
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

	private static <T> List<T> shuffle(List<T> tList) {
		var newArrayList = new ArrayList<T>(tList);
		Collections.shuffle(newArrayList);
		return newArrayList;
	}

	private static URI buildUriFromServiceInstance(ServiceInstance server,
			URI originalRequestUrl) {
		return URI.create(originalRequestUrl.getScheme() + "://" + server.getHost() + ':'
				+ server.getPort() + originalRequestUrl.getPath());
	}

}
