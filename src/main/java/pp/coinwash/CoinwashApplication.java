package pp.coinwash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CoinwashApplication {

	public static void main(String[] args) {
		SpringApplication.run(CoinwashApplication.class, args);
	}

}
