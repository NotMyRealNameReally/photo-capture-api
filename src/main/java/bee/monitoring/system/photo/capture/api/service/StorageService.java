package bee.monitoring.system.photo.capture.api.service;

import bee.monitoring.system.photo.capture.api.config.MountedStorageConfigurationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;

@Service
public class StorageService {

    private final String basePath;
    private final long maxSizeBytes;

    private static final Logger LOG = LoggerFactory.getLogger(StorageService.class);

    @Autowired
    public StorageService(MountedStorageConfigurationProperties props) {
        this.basePath = props.basePath();
        this.maxSizeBytes = props.maxSizeBytes();
    }

    public void archiveFile(String fileName, String hiveId, LocalDate date, byte[] bytes) throws IOException {
        var dir = Files.createDirectories(Path.of(basePath, hiveId, date.toString()));
        Files.write(dir.resolve(fileName), bytes);
    }

    public Optional<byte[]> getFile(String hiveId, LocalDate date, String fileName) throws IOException {
        var path = Path.of(basePath, hiveId, date.toString(), fileName);
        if (Files.exists(path)) {
            return Optional.of(Files.readAllBytes(path));
        }
        return Optional.empty();
    }

    @Scheduled(cron = "${archive.mounted.cleanupCron}")
    void cleanup() throws IOException {
        LOG.info("Starting cleanup");
        var archiveSize = calculateDirectorySize(Path.of(basePath));
        if (archiveSize < maxSizeBytes) {
            LOG.info("Current archive size does not exceed limit");
            return;
        }
        var requiredToRemove = archiveSize - maxSizeBytes;
        var removed = 0L;
        var it = listDateDirectoriesWithSizes().entrySet().iterator();

        while (it.hasNext() && removed < requiredToRemove) {
            var entry = it.next();
            deleteDirectory(entry.getKey());
            removed += entry.getValue();
            LOG.info("Removed directory: {}", entry.getKey().toString());
        }
    }

    private long calculateDirectorySize(Path dir) {
        try (var files = Files.walk(dir)) {
            return files
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            return 0L;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            LOG.error("Couldn't calculate directory size", e);
            return 0L;
        }
    }

    private SortedMap<Path, Long> listDateDirectoriesWithSizes() throws IOException {
        var result = new TreeMap<Path, Long>(Comparator.comparing(p -> p.getFileName().toString()));

        try (var hives = Files.list(Path.of(basePath))) {
            hives.filter(Files::isDirectory).forEach(hive -> {
                try (Stream<Path> dates = Files.list(hive)) {
                    dates.filter(Files::isDirectory)
                            .forEach(dateDir -> result.put(dateDir, calculateDirectorySize(dateDir)));
                } catch (IOException e) {
                    LOG.error("Couldn't list all directories", e);
                }
            });
        }
        return result;
    }

    void deleteDirectory(Path dir) throws IOException {
        try (Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(Comparator.reverseOrder()) // reverse order to delete the parent directory at the end
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException ignored) {
                        }
                    });
        }
    }
}
