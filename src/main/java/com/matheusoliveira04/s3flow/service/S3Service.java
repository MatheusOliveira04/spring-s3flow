package com.matheusoliveira04.s3flow.service;

import com.matheusoliveira04.s3flow.exceptions.FileNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.List;

@Service
public class S3Service {

    private S3Client s3Client;

    @Value("${aws.bucket.name}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public void uploadFile(MultipartFile file) throws IOException {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(file.getOriginalFilename())
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );
    }

    public Resource downloadFile(String key) {
        ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build()
        );
        return new ByteArrayResource(objectAsBytes.asByteArray());
    }

    public void deleteFile(String key) {
        DeleteObjectRequest build = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();
        s3Client.deleteObject(build);
    }

    public List<String> listAll() {
        ListObjectsV2Request request = buildListRequest(bucketName);
        ListObjectsV2Response response = executeListRequest(request);

        validateNotEmptyS3FileList(response);
        return extractKeyFile(response);
    }

    private ListObjectsV2Request buildListRequest(String bucketName) {
        return ListObjectsV2Request.builder()
                .bucket(bucketName)
                .build();
    }

    private ListObjectsV2Response executeListRequest(ListObjectsV2Request request) {
        return s3Client.listObjectsV2(request);
    }

    private static List<String> extractKeyFile(ListObjectsV2Response listObjectsV2Response) {
        return listObjectsV2Response.contents().stream().map(S3Object::key).toList();
    }

    private static void validateNotEmptyS3FileList(ListObjectsV2Response responseObjectList) {
        List<S3Object> contents = responseObjectList.contents();
        if (contents == null || contents.isEmpty()) {
            throw new FileNotFoundException("No files found in S3 bucket.");
        }
    }
}
