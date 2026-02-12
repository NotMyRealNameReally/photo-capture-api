package bee.monitoring.system.photo.capture.api.repository;

import bee.monitoring.system.photo.capture.api.entity.BeeDetection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BeeDetectionRepository extends JpaRepository<BeeDetection, Integer> {
}
