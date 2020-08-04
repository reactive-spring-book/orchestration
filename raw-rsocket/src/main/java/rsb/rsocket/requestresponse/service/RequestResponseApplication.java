package rsb.rsocket.requestresponse.service;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RequestResponseApplication {

	@SneakyThrows
	public static void main(String[] arrrImAPirate) {
		SpringApplication.run(RequestResponseApplication.class, arrrImAPirate);
		System.in.read();// <1>
	}

}
