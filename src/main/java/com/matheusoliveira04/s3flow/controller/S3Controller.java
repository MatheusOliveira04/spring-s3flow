package com.matheusoliveira04.s3flow.controller;

import com.matheusoliveira04.s3flow.service.S3Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class S3Controller {

    private S3Service s3Service;

    public S3Controller(S3Service s3Service) {
        this.s3Service = s3Service;
    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam MultipartFile file) throws IOException {
        s3Service.uploadFile(file);
        return ResponseEntity.ok("File uploaded successfully");
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> download(@PathVariable String filename) {
        byte[] data = s3Service.downloadFile(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(data);
    }

    @DeleteMapping("/delete/{filename}")
    public ResponseEntity<String> delete(@PathVariable String filename) {
        s3Service.deleteFile(filename);
        return ResponseEntity.ok("File deleted successfully. Filename: " + filename);
    }

}
