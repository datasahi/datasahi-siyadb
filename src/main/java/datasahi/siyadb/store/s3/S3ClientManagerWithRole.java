package datasahi.siyadb.store.s3;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import datasahi.siyadb.common.file.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.*;

public class S3ClientManagerWithRole implements S3ClientManager {

    private static final Logger log = LoggerFactory.getLogger(S3ClientManagerWithRole.class);
    private AmazonS3 s3Client;
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
    public AmazonS3 getS3Client() {

        String roleArn = System.getenv("AWS_ROLE_ARN");
        if (roleArn == null || roleArn.isEmpty()) {
            roleArn = s3Config.getRoleArn();
        }

        String sessionName = System.getenv("AWS_ROLE_SESSION_NAME");
        if (sessionName == null || sessionName.isEmpty()) {
            sessionName = "default_session";
        }

        return getS3Client(roleArn, sessionName);
    }

    private AmazonS3 getS3Client(String roleArn, String roleSessionName) {

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

            return getAmazonS3ClientForRoleArn(roleArn, roleSessionName, tokenData);
        }
    }

    private synchronized AmazonS3 getAmazonS3ClientForRoleArn(String roleArn, String roleSessionName,
                                                              String tokenData) {

        if (s3Client != null) {
            return s3Client;
        }
        try {
            StsClient stsClient = StsClient.builder()
                    .region(Region.AWS_GLOBAL)
                    .build();

            Credentials credentials = null;
            if (tokenData != null && !tokenData.isEmpty()) {
                credentials = assumeWebIdentity(stsClient, roleArn, roleSessionName, tokenData);
            } else {
                credentials = assumeGivenRole(stsClient, roleArn, roleSessionName);
            }

            if (credentials == null) {
                throw new RuntimeException("Unable to get aws credentials");
            }

            BasicSessionCredentials awsCredentials = new BasicSessionCredentials(
                    credentials.accessKeyId(),
                    credentials.secretAccessKey(),
                    credentials.sessionToken());

            this.s3Client = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(s3Config.getRegion())
                    .build();
            this.clientCreatedAt = System.currentTimeMillis();
            return s3Client;
        } catch (AmazonServiceException e) {
            this.s3Client = null;
            throw new IllegalStateException("Could not prepare S3 client for role :: " + roleArn, e);
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
