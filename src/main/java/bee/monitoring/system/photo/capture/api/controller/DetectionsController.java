package bee.monitoring.system.photo.capture.api.controller;

import bee.monitoring.system.photo.capture.api.repository.BeeDetectionRepository;
import bee.monitoring.system.photo.capture.api.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1/detections")
public class DetectionsController {

    private final BeeDetectionRepository repository;
    private final StorageService storageService;

    @Autowired
    public DetectionsController(BeeDetectionRepository repository, StorageService storageService) {
        this.repository = repository;
        this.storageService = storageService;
    }

    @GetMapping
    public List<BeeDetectionDto> getAll() {
        return repository
                .findAll()
                .stream()
                .map(detection -> new BeeDetectionDto(detection.getId(), detection.getTimestamp(), detection.getBeeCoverage(), detection.getHiveId()))
                .toList();
    }

    @GetMapping(path = "/image/{imageId}", produces = MediaType.IMAGE_JPEG_VALUE)
    public @ResponseBody byte[] getImageById(@PathVariable final int imageId) {
        try {
            var detection = repository.findById(imageId)
                    .orElseThrow();
            return storageService.getFile(detection.getHiveId(), detection.getTimestamp().toLocalDate(), detection.getFileName())
                    .orElseThrow();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server error occurred", e);
        }
    }
}
