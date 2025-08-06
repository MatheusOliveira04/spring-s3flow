package com.matheusoliveira04.s3flow.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    S3Client s3Client;

    @InjectMocks
    S3Service s3Service;

    @Captor
    ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor;

    @Captor
    ArgumentCaptor<RequestBody> requestBodyCaptor;

    @Nested
    class uploadFile {

        @Test
        @DisplayName("should call PutObject on S3Client with success")
        void shouldCallPutObjectOnS3Client() throws IOException {
            String fileName = "testFile.txt";
            byte[] content = "testing content file".getBytes();
            MultipartFile file = new MockMultipartFile("file", fileName, "text/plain", content);

            s3Service.uploadFile(file);

            verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        }

        @Test
        @DisplayName("should capture PutObject arguments")
        void shouldCapturePutObjectArguments() throws IOException {
            byte[] content = "Testing content".getBytes();
            MultipartFile file = new MockMultipartFile("file", "file.txt",
                    "text/plain", content);

            s3Service.uploadFile(file);

            verify(s3Client).putObject(putObjectRequestCaptor.capture(), requestBodyCaptor.capture());

            var putObjectRequestCaptured = putObjectRequestCaptor.getValue();
            var requestBodyCaptured = requestBodyCaptor.getValue();

            assertEquals("file.txt", putObjectRequestCaptured.key());
            try (InputStream is = requestBodyCaptured.contentStreamProvider().newStream()) {
                assertArrayEquals(content, is.readAllBytes());
            }
        }

    }
}