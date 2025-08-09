package com.matheusoliveira04.s3flow.controller;

import com.matheusoliveira04.s3flow.service.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ControllerTest {

    @Mock
    S3Service s3Service;

    @InjectMocks
    S3Controller s3Controller;

    @Captor
    ArgumentCaptor<MultipartFile> fileCaptor;

    @Nested
    class upload {

        @Test
        @DisplayName("should return 200 OK with success message after file upload")
        void shouldReturn200OkWithMessageAfterFileUpload() throws IOException {
            var successMessage = "File uploaded successfully";

            byte[] content = "Testing content".getBytes();
            MultipartFile file = new MockMultipartFile("file", "file.txt",
                    "text/plain", content);

            var response = s3Controller.upload(file);

            assertNotNull(response);
            assertNotNull(response.getBody());
            assertEquals(successMessage, response.getBody());
            assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());

            verify(s3Service, times(1)).uploadFile(any());
        }

        @Test
        @DisplayName("should call uploadFile on S3Service")
        void shouldCallUploadFileOnS3Service() throws IOException {
            byte[] content = "Testing content".getBytes();
            MultipartFile file = new MockMultipartFile("file", "file.txt",
                    "text/plain", content);

            doNothing().when(s3Service).uploadFile(any());

            s3Controller.upload(file);

            verify(s3Service, times(1)).uploadFile(any());
        }

        @Test
        @DisplayName("should capture UploadFile arguments on S3Service")
        void shouldCaptureUploadFileArgumentsOnS3Service() throws IOException {
            byte[] content = "Testing content".getBytes();
            MultipartFile file = new MockMultipartFile("file", "file.txt",
                    "text/plain", content);

            doNothing().when(s3Service).uploadFile(fileCaptor.capture());

            s3Controller.upload(file);

            var fileCaptured = fileCaptor.getValue();
            assertNotNull(fileCaptured);
            assertEquals(file.getName(), fileCaptured.getName());
            assertEquals(file.getOriginalFilename(), fileCaptured.getOriginalFilename());
            assertEquals(file.getContentType(), fileCaptured.getContentType());
            assertEquals(file.getBytes(), fileCaptured.getBytes());

            verify(s3Service, times(1)).uploadFile(file);
        }

    }

    @Nested
    class download {

        @Test
        @DisplayName("should return 200 OK, Content Disposition with message after file download")
        void shouldReturn200OkWithMessageAfterFileDownload() throws IOException {
            var filename = "fileTesting";
            byte[] content = "Testing content".getBytes();
            Resource resource = new ByteArrayResource(content);

            doReturn(resource).when(s3Service).downloadFile(any());

            var response = s3Controller.download(filename);

            assertNotNull(response);
            assertNotNull(response.getBody());
            assertArrayEquals(content, response.getBody().getInputStream().readAllBytes());
            assertEquals(HttpStatus.OK.value(), response.getStatusCode().value());
            assertEquals("attachment; filename=" + filename, response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));

            verify(s3Service, times(1)).downloadFile(eq(filename));
        }

        @Test
        @DisplayName("should call DownloadFile on S3Service")
        void shouldCallDownloadFileOnS3Service() {
            var filename = "fileTesting";
            byte[] content = "Testing content".getBytes();
            Resource resource = new ByteArrayResource(content);

            doReturn(resource).when(s3Service).downloadFile(any());

            s3Controller.download(filename);

            verify(s3Service, times(1)).downloadFile(any());
        }

        @Test
        @DisplayName("should capture DownloadFile on S3Service")
        void shouldCaptureDownloadFileOnS3Service() {
            var filename = "fileTesting";
            byte[] content = "Testing content".getBytes();
            Resource resource = new ByteArrayResource(content);

            doReturn(resource).when(s3Service).downloadFile(stringCaptor.capture());

            s3Controller.download(filename);

            assertEquals(filename, stringCaptor.getValue());

            verify(s3Service, times(1)).downloadFile(eq(filename));
        }
    }
}