package bee.monitoring.system.photo.capture.api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity(name = "bee_detections")
public class BeeDetection {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String hiveId;
    @Column(name="filename")
    private String fileName;
    private LocalDateTime timestamp;
    private double beeCoverage;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getHiveId() {
        return hiveId;
    }

    public void setHiveId(String hiveId) {
        this.hiveId = hiveId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public double getBeeCoverage() {
        return beeCoverage;
    }

    public void setBeeCoverage(double beeCoverage) {
        this.beeCoverage = beeCoverage;
    }
}
