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

    @Captor
    ArgumentCaptor<DeleteObjectRequest> deleteObjectRequestCaptor;
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

    @Nested
    class deleteFile {

        @Test
        @DisplayName("should call DeleteObject on S3Client")
        void shouldCallDeleteObjectOnS3Client() {
            var fileName = "file.txt";

            s3Service.deleteFile(fileName);

            verify(s3Client, times(1)).deleteObject(any(DeleteObjectRequest.class));
        }

        @Test
        @DisplayName("should capture DeleteObject arguments")
        void shouldCaptureDeleteObjectArguments() {
            var fileName = "file.txt";

            s3Service.deleteFile(fileName);

            verify(s3Client, times(1)).deleteObject(deleteObjectRequestCaptor.capture());

            var deleteObjectRequestCaptured = deleteObjectRequestCaptor.getValue();
            assertEquals(fileName, deleteObjectRequestCaptured.key());
        }

    }

    @Nested
    class listAll {

        @Test
        @DisplayName("should return String List with success")
        void shouldReturnStringListWithSuccess() {
            List<S3Object> fileNames = List.of(
                    S3Object.builder().key("file1.txt").build(),
                    S3Object.builder().key("file2.txt").build()
            );

            ListObjectsV2Response mockResponse = ListObjectsV2Response.builder().contents(fileNames).build();

            doReturn(mockResponse).when(s3Client).listObjectsV2(any(ListObjectsV2Request.class));

            var output = s3Service.listAll();

            assertNotNull(output);
            assertEquals(output.size(), fileNames.stream().map(S3Object::key).toList().size());
            assertArrayEquals(output.toArray(), fileNames.stream().map(S3Object::key).toList().toArray());
        }

        @Test
        @DisplayName("should call ListObject on S3Client")
        void shouldCallListObjectOnS3Client() {
            List<S3Object> fileNames = List.of(
                    S3Object.builder().key("file1.txt").build(),
                    S3Object.builder().key("file2.txt").build()
            );
            ListObjectsV2Response mockResponse = ListObjectsV2Response.builder().contents(fileNames).build();

            doReturn(mockResponse).when(s3Client).listObjectsV2(any(ListObjectsV2Request.class));

            s3Service.listAll();

            verify(s3Client, times(1)).listObjectsV2(any(ListObjectsV2Request.class));
        }

        @Test
        @DisplayName("should capture ListObject arguments")
        void shouldCaptureListObjectArguments() {
            List<S3Object> fileNames = List.of(
                    S3Object.builder().key("file1.txt").build(),
                    S3Object.builder().key("file2.txt").build()
            );
            ListObjectsV2Response mockResponse = ListObjectsV2Response.builder().contents(fileNames).build();

            doReturn(mockResponse).when(s3Client).listObjectsV2(any(ListObjectsV2Request.class));

            s3Service.listAll();

            verify(s3Client, times(1)).listObjectsV2(listObjectsV2RequestCaptor.capture());

            var listObjectsV2RequestCaptured = listObjectsV2RequestCaptor.getValue();
            assertNotNull(listObjectsV2RequestCaptured);
        }

    }

}