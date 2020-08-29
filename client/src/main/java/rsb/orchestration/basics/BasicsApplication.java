package rsb.orchestration.basics;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class BasicsApplication {

	public static void main(String args[]) {
		SpringApplication.run(BasicsApplication.class, args);
	}

}
