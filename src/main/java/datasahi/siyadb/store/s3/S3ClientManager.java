package datasahi.siyadb.store.s3;

import software.amazon.awssdk.services.s3.S3Client;

public interface S3ClientManager {
    S3Client getS3Client();
}