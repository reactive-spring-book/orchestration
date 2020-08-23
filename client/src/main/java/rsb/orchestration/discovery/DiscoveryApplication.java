package rsb.orchestration.discovery;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;

@Log4j2
@SpringBootApplication
public class DiscoveryApplication {

	public static void main(String args[]) {
		SpringApplication.run(DiscoveryApplication.class, args);
	}

	@EventListener
	public void any(ApplicationEvent event) {
		log.info(event.toString());
	}

}
