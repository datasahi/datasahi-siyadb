package datasahi.siyadb.store.s3;

import datasahi.siyadb.common.file.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.auth.StsAssumeRoleCredentialsProvider;
import software.amazon.awssdk.services.sts.model.*;

public class S3ClientManagerWithRole implements S3ClientManager {

    private static final Logger log = LoggerFactory.getLogger(S3ClientManagerWithRole.class);

    private S3Client s3Client;
    private S3Config s3Config;

    private long clientCreatedAt;
    private long expiryMillisForClient = 10 * 60 * 1000; // 10 mins - can be from config

    public S3ClientManagerWithRole(S3Config s3Config) {
        this.s3Config = s3Config;
        if (s3Config.getRefreshClientInSeconds() > 0) {
            this.expiryMillisForClient = s3Config.getRefreshClientInSeconds() * 1000;
        }
    }

    @Override
    public S3Client getS3Client() {

        synchronized (this) {
            if ((System.currentTimeMillis() - clientCreatedAt) >= expiryMillisForClient) {
                s3Client = null;
            }
            if (s3Client != null) {
                return s3Client;
            }

            String webIdentityTokenFile = System.getenv("AWS_WEB_IDENTITY_TOKEN_FILE");
            String tokenData = null;
            if (webIdentityTokenFile != null) {
                try {
                    tokenData = new FileUtil().readFile(webIdentityTokenFile);
                } catch (Exception e) {
                    log.error("Token data not present at " + webIdentityTokenFile, e);
                }
            }

            this.s3Client = getAmazonS3ClientForRoleArn(tokenData);
            return s3Client;
        }
    }

    private synchronized S3Client getAmazonS3ClientForRoleArn(String tokenData) {

        if (s3Client != null) {
            return s3Client;
        }

        try {
            StsClient stsClient = StsClient.builder()
                    .region(Region.of(s3Config.getRegion()))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();

            AssumeRoleRequest.Builder assumeRoleRequestBuilder = AssumeRoleRequest.builder()
                    .roleArn(s3Config.getRoleArn())
                    .roleSessionName("S3FileStoreSession");
            if (tokenData != null && !tokenData.isEmpty()) {
                assumeRoleRequestBuilder.tokenCode(tokenData);
            }
            AssumeRoleRequest assumeRoleRequest = assumeRoleRequestBuilder
                    .build();

            AwsCredentialsProvider credentialsProvider = StsAssumeRoleCredentialsProvider.builder()
                    .stsClient(stsClient)
                    .refreshRequest(assumeRoleRequest)
                    .build();


            S3Client s3Client = S3Client.builder()
                    .region(Region.of(s3Config.getRegion()))
                    .credentialsProvider(credentialsProvider)
                    .build();
            this.clientCreatedAt = System.currentTimeMillis();
            return s3Client;
        } catch (Exception e) {
            this.s3Client = null;
            throw new IllegalStateException("Could not prepare S3 client for role :: " + s3Config.getRoleArn(), e);
        }
    }

    private Credentials assumeGivenRole(StsClient stsClient, String roleArn, String roleSessionName) {

        try {
            AssumeRoleRequest roleRequest = AssumeRoleRequest.builder()
                    .roleArn(roleArn)
                    .roleSessionName(roleSessionName)
                    .build();

            AssumeRoleResponse roleResponse = stsClient.assumeRole(roleRequest);

            return roleResponse.credentials();

        } catch (StsException e) {
            throw new IllegalStateException("Could not prepare S3 client for role :: " + roleArn, e);
        }
    }

    private Credentials assumeWebIdentity(StsClient stsClient, String roleArn, String roleSessionName,
                                          String tokenData) {

        try {
            AssumeRoleWithWebIdentityRequest roleRequest = AssumeRoleWithWebIdentityRequest.builder()
                    .roleArn(roleArn)
                    .roleSessionName(roleSessionName)
                    .webIdentityToken(tokenData)
                    .build();

            AssumeRoleWithWebIdentityResponse response = stsClient.assumeRoleWithWebIdentity(roleRequest);
            return response.credentials();
        } catch (StsException e) {
            throw new IllegalStateException("Could not prepare S3 client for web identity :: " + roleArn, e);
        }
    }
}
