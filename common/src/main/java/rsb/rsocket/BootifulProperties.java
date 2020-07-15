package rsb.rsocket;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("bootiful")
public class BootifulProperties {

	private final RSocket rsocket = new RSocket();

	@Data
	public static class RSocket {

		private String hostname = "localhost";

		private int port = 8181;

	}

}
