package datasahi.siyadb.store.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.commons.lang3.StringUtils;

public class S3ClientManagerWithKeys implements S3ClientManager {

    private AmazonS3 s3Client;
    private S3Config s3Config;

    public S3ClientManagerWithKeys(S3Config s3Config) {
        this.s3Config = s3Config;
        this.s3Client = createClient();
    }

    @Override
    public AmazonS3 getS3Client() {

        return s3Client;
    }

    private synchronized AmazonS3 createClient() {

        if (s3Client != null) {
            return s3Client;
        }

        try {
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(s3Config.getAccessKey(), s3Config.getSecretKey());
            if (StringUtils.isNotEmpty(s3Config.getEndpointUrl())) {
                AwsClientBuilder.EndpointConfiguration endpointConfig =
                        new AwsClientBuilder.EndpointConfiguration(s3Config.getEndpointUrl(), s3Config.getRegion());
                AmazonS3ClientBuilder clientBuilder = AmazonS3ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                        .withEndpointConfiguration(endpointConfig)
                        .withPathStyleAccessEnabled(s3Config.isPathStyleAccess());
                if (!s3Config.isHttps()) {
                    clientBuilder.setClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP));
                }
                return clientBuilder.build();
            } else {
                return AmazonS3ClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                        .withRegion(s3Config.getRegion())
                        .build();
            }
        } catch (AmazonServiceException e) {
            this.s3Client = null;
            throw new IllegalStateException("Could not prepare S3 client for keys", e);
        }
    }
}
