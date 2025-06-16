package datasahi.siyadb.store.s3;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;

public class S3ClientManagerWithKeys implements S3ClientManager {

    private final S3Client s3Client;
    private S3Config s3Config;

    public S3ClientManagerWithKeys(S3Config s3Config) {
        this.s3Config = s3Config;
        this.s3Client = createClient();
    }

    private synchronized S3Client createClient() {

        try {

            AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                    s3Config.getAccessKey(),
                    s3Config.getSecretKey());

            S3ClientBuilder s3ClientBuilder = S3Client.builder()
                    .region(Region.of(s3Config.getRegion()))
                    .credentialsProvider(StaticCredentialsProvider.create(awsCreds));

            if (StringUtils.isNotEmpty(s3Config.getEndpointUrl())) {
                S3Configuration serviceConfiguration = S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build();
                s3ClientBuilder.endpointOverride(URI.create(s3Config.getEndpointUrl()))
                        .serviceConfiguration(serviceConfiguration);

/*              todo http is to be handled
                if (!s3Config.isHttps()) {
                    SdkHttpClient httpClient = ApacheHttpClient.builder()
                            .buildWithDefaults(
                                    SdkHttpClient.builder()
                                            .protocol(useHttps ? "https" : "http")
                                            .build()
                            );
                }
*/
            }
            return s3ClientBuilder.build();
        } catch (S3Exception e) {
            throw new IllegalStateException("Could not prepare S3 client for keys", e);
        }
    }

    @Override
    public S3Client getS3Client() {
        return s3Client;
    }
}
