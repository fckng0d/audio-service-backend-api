package me.fckng0d.audioservicebackend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.UUID;

@Service
public class S3Service {

    private AmazonS3 s3client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3Service(AmazonS3 s3client) {
        this.s3client = s3client;
    }

    public String uploadFile(String folderName, MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();

        if (fileName == null) {
            throw new RemoteException("FileName cannot be null");
        }

        String keyName = folderName
                .concat("/")
                .concat(generateUniqueFileName(fileName));

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());

        s3client.putObject(new PutObjectRequest(bucketName, keyName, file.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        return s3client.getUrl(bucketName, keyName).toString();
    }

    public String generateUniqueFileName(String fileName) {
        String originalFileName = fileName.substring(0, fileName.lastIndexOf("."));
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));

        String uniqueAddition = UUID.randomUUID().toString();

        return originalFileName
                .concat("_")
                .concat(uniqueAddition)
                .concat(".")
                .concat(fileExtension);
    }

    public S3Object getFile(String keyName) {
        return s3client.getObject(bucketName, keyName);
    }

//    public String getUrl(String bucketName, String keyName) {
//
//    }

}