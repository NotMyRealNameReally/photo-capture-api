package bee.monitoring.system.photo.capture.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class, UserDetailsServiceAutoConfiguration.class})
@EnableScheduling
@ConfigurationPropertiesScan
public class PhotoCaptureApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhotoCaptureApiApplication.class, args);
	}

}
