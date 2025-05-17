package datasahi.siyadb.store.s3;

import com.amazonaws.services.s3.AmazonS3;

public interface S3ClientManager {
    AmazonS3 getS3Client();
}
