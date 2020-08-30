package rsb.orchestration.hedging;

import org.springframework.cloud.client.ServiceInstance;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

abstract public class HedgingUtils {

	public static <T> List<T> shuffle(List<T> tList) {
		var newArrayList = new ArrayList<T>(tList);
		Collections.shuffle(newArrayList);
		return newArrayList;
	}

	public static URI buildUriFromServiceInstance(ServiceInstance server,
			URI originalRequestUrl) {
		return URI.create(originalRequestUrl.getScheme() + "://" + server.getHost() + ':'
				+ server.getPort() + originalRequestUrl.getPath());
	}

}
