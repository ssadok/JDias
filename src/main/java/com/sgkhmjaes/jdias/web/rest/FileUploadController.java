package com.sgkhmjaes.jdias.web.rest;

import com.sgkhmjaes.jdias.config.Constants;
import com.sgkhmjaes.jdias.domain.Photo;
import com.sgkhmjaes.jdias.service.PhotoService;
import com.sgkhmjaes.jdias.service.StorageService;
import com.sgkhmjaes.jdias.storage.StorageFileNotFoundException;
import com.sgkhmjaes.jdias.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.inject.Inject;
import javax.servlet.annotation.MultipartConfig;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    private final StorageService storageService;
    private final PhotoService photoService;

    private static final String SMALL_PREFIX = "thumb_small_";
    private static final String MEDIUM_PREFIX = "thumb_medium_";
    private static final String LARGE_PREFIX = "thumb_large_";
    private static final String SCALED_FULL_PREFIX = "/scaled_full_";

    private final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    public FileUploadController(StorageService storageService, PhotoService photoService) {
        this.storageService = storageService;
        this.photoService = photoService;
    }

    @GetMapping("/files")
    public String listUploadedFiles(Model model) throws IOException {

        model.addAttribute("files", storageService
                .loadAll()
                .map(path ->
                        MvcUriComponentsBuilder
                                .fromMethodName(FileUploadController.class, "serveFile", path.getFileName().toString())
                                .build().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+file.getFilename()+"\"")
                .body(file);
    }

    @PostMapping(value = "/file", consumes = "multipart/form-data")
    public ResponseEntity<Photo[]> handleFileUpload(@RequestParam("file") MultipartFile[] multipartFiles, RedirectAttributes redirectAttributes) throws URISyntaxException, IOException {
        Photo[] photos = new Photo[multipartFiles.length];
        for (int i = 0; i<multipartFiles.length; i++) {
            File file = storageService.store(multipartFiles[i]);
            redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + multipartFiles[i].getOriginalFilename() + "!");

            photos[i] = photoService.save(file);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("PHOTOS", "UPLOADED"))
            .body(photos);
    }

    @DeleteMapping("/files/{filename:.+}")
    public ResponseEntity<Void> deleteImage(@PathVariable String filename) {
        log.debug("REST request to delete Image : {}", filename);
        storageService.deleteImage(filename);
        storageService.deleteImage(Constants.SMALL_PREFIX + filename);
        storageService.deleteImage(Constants.MEDIUM_PREFIX + filename);
        storageService.deleteImage(Constants.LARGE_PREFIX + filename);
        storageService.deleteImage(Constants.SCALED_FULL_PREFIX + filename);

        photoService.delete(filename);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("PHOTO", filename)).build();
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
