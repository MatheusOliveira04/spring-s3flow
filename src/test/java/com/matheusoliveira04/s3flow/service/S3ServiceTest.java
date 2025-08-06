package com.matheusoliveira04.s3flow.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
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

    @Captor
    ArgumentCaptor<GetObjectRequest> getObjectRequestCaptor;

    @Nested
    class uploadFile {

        @Test
        @DisplayName("should call PutObject on S3Client")
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

    @Nested
    class downloadFile {

        @Test
        @DisplayName("should return Resource with success")
        void shouldReturnResourceWithSuccess() throws IOException {
            var fileName = "file.txt";
            byte[] content = "Testing content".getBytes();
            ResponseBytes<GetObjectResponse> responseBytes =
                    ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), content);

            doReturn(responseBytes).when(s3Client).getObjectAsBytes(any(GetObjectRequest.class));

            var output = s3Service.downloadFile(fileName);

            assertNotNull(output);
            assertArrayEquals(content, output.getContentAsByteArray());
        }

        @Test
        @DisplayName("should call GetObject on S3Client")
        void shouldCallGetObjectOnS3Client() {
            var fileName = "file.txt";
            byte[] content = "Testing content".getBytes();
            ResponseBytes<GetObjectResponse> responseBytes =
                    ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), content);

            doReturn(responseBytes).when(s3Client).getObjectAsBytes(any(GetObjectRequest.class));


            s3Service.downloadFile(fileName);

            verify(s3Client, times(1)).getObjectAsBytes(any(GetObjectRequest.class));
        }

        @Test
        @DisplayName("should capture GetObject arguments")
        void shouldCaptureGetObjectArguments() {
            byte[] content = "Testing content".getBytes();
            var fileName = "file.txt";
            ResponseBytes<GetObjectResponse> responseBytes =
                    ResponseBytes.fromByteArray(GetObjectResponse.builder().build(), content);

            doReturn(responseBytes).when(s3Client).getObjectAsBytes(any(GetObjectRequest.class));

            s3Service.downloadFile(fileName);

            verify(s3Client).getObjectAsBytes(getObjectRequestCaptor.capture());

            var getObjectRequestCaptured = getObjectRequestCaptor.getValue();
            assertEquals(fileName, getObjectRequestCaptured.key());
        }

    }
}