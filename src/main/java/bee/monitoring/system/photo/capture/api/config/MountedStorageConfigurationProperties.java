package bee.monitoring.system.photo.capture.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "archive.mounted")
public record MountedStorageConfigurationProperties(
        String basePath,
        long maxSizeBytes,
        String cleanupCron
) {
}
