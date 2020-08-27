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

	private final int timeoutInSeconds = 10;

	private final ReactiveDiscoveryClient reactiveDiscoveryClient;

	private final int maxNodes;

	@Override
	public Mono<ClientResponse> filter(ClientRequest clientRequest,
			ExchangeFunction exchangeFunction) {
		var requestUrl = clientRequest.url();
		var apiName = requestUrl.getHost();
		return this.reactiveDiscoveryClient //
				.getInstances(apiName) //
				.collectList().map(HedgingExchangeFilterFunction::shuffle)
				.flatMapMany(Flux::fromIterable).take(maxNodes)
				.map(si -> buildUriFromServiceInstance(si, requestUrl)) //
				.map(URI::create) //
				.map(uri -> invoke(uri, clientRequest, exchangeFunction)) //
				.collectList() //
				.flatMap(list -> Flux.first(list)
						.timeout(Duration.ofSeconds(timeoutInSeconds)).singleOrEmpty()) //
				.doOnTerminate(() -> {
					if (log.isDebugEnabled()) {
						log.debug("finished the hedging chain for " + apiName + '.');
					}
				});
	}

	/*
	 * to avoid dogpiling on the first N instances to be returned from the service
	 * registry...
	 */
	private static <T> List<T> shuffle(List<T> tList) {
		var newArrayList = new ArrayList<T>(tList);
		Collections.shuffle(newArrayList);
		return newArrayList;
	}

	private static String buildUriFromServiceInstance(ServiceInstance server,
			URI requestUrl) {
		var downstreamUrl = (requestUrl.getScheme() + "://" + server.getHost() + ':'
				+ server.getPort() + requestUrl.getPath());
		if (log.isDebugEnabled()) {
			log.debug("the proposed output URI is " + downstreamUrl);
		}
		return downstreamUrl;
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
