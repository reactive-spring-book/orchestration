package rsb.orchestration.gateway;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Log4j2
@SpringBootApplication
public class GatewayApplication {

	public static void main(String args[]) {
		System.setProperty("server.port", "8080");
		SpringApplication.run(GatewayApplication.class, args);
	}

}
