package pp.coinwash.config.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

	@Bean(name = "historyTaskExecutor")
	public TaskExecutor historyTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(5);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("History-");
		executor.initialize();
		return executor;
	}

	@Bean(name = "machineTaskExecutor")
	public TaskExecutor MachineTaskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(2);
		executor.setMaxPoolSize(5);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("Machine-");
		executor.initialize();
		return executor;
	}

}
