package bee.monitoring.system.photo.capture.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.api-key}")
    private String apiKey;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests -> requests
                        .anyRequest()
                        .access(((authentication, ctx) -> {
                            if (ctx.getRequest().getRequestURI().startsWith("/api/v1/detections")) {
                                return new AuthorizationDecision(true);
                            }
                            var providedKey = ctx.getRequest()
                                    .getHeader("X-API-KEY");
                            var authorized = providedKey != null && providedKey.equals(apiKey);
                            return new AuthorizationDecision(authorized);
                        })))
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
}
