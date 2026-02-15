package bee.monitoring.system.photo.capture.api.controller;

import bee.monitoring.system.photo.capture.api.config.RabbitConfig;
import bee.monitoring.system.photo.capture.api.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;


@RestController
@RequestMapping("/api/v1/photo")
public class PhotoCaptureController {

    private final RabbitTemplate rabbitTemplate;
    private final StorageService storageService;

    private final Logger LOG = LoggerFactory.getLogger(PhotoCaptureController.class);

    @Autowired
    public PhotoCaptureController(RabbitTemplate rabbitTemplate, StorageService storageService) {
        this.rabbitTemplate = rabbitTemplate;
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestPart("file") MultipartFile file, @RequestHeader(name = "hiveId") String hiveId) throws IOException {
        var fileName = Objects.requireNonNull(file.getOriginalFilename());
        LOG.info("Received photo: {}", fileName);
        var fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(fileNameWithoutExtension, DateTimeFormatter.ofPattern("uuuuMMdd_HHmmss"));
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid filename date format: " + file.getOriginalFilename(), e);
        }
        if (!StringUtils.hasText(hiveId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing hiveId header");
        }
        var props = new MessageProperties();
        props.setHeader("filename", fileName);
        props.setHeader("timestamp", dateTime);
        props.setHeader("hiveId", hiveId);
        props.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        var message = new Message(file.getBytes(), props);

        rabbitTemplate.send(RabbitConfig.PHOTO_QUEUE, message);
        storageService.archiveFile(fileName, hiveId, dateTime.toLocalDate(), file.getBytes());
        LOG.info("Sent {} successfully", file.getOriginalFilename());
        return ResponseEntity.ok().build();
    }
}
