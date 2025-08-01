package com.matheusoliveira04.s3flow.controller;

import com.matheusoliveira04.s3flow.service.S3Service;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

}
