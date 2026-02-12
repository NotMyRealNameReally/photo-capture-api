package bee.monitoring.system.photo.capture.api.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String PHOTO_QUEUE = "photo-uploads";

    @Bean
    public Queue photoQueue() {
        return new Queue(PHOTO_QUEUE, true);
    }
}
