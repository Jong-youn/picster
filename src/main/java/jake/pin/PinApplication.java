package jake.pin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class PinApplication {

	public static void main(String[] args) {
		SpringApplication.run(PinApplication.class, args);
	}

}
