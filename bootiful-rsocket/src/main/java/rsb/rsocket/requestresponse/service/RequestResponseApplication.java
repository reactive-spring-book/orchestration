package rsb.rsocket.requestresponse.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RequestResponseApplication {

	public static void main(String[] arrrImAPirate) {
		System.setProperty("spring.profiles.active", "service");
		SpringApplication.run(RequestResponseApplication.class, arrrImAPirate);
	}

}
