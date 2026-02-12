package bee.monitoring.system.photo.capture.api.controller;

import java.time.LocalDateTime;

public record BeeDetectionDto (
        int id,
        LocalDateTime timestamp,
        double coverage,
        String hiveId
) {

}
