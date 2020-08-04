package rsb.rsocket;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("bootiful") // <1>
public class BootifulProperties {

	private final RSocket rsocket = new RSocket();

	@Data
	public static class RSocket {

		private String hostname = "localhost"; // <2>

		private int port = 8182; // <3>

	}

}
