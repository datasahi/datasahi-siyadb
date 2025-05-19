package datasahi.siyadb.store.s3;

import datasahi.siyadb.store.StoreConfig;

public class S3Config extends StoreConfig {

    public enum SecurityMode {
        ROLE_ARN,
        ACCESS_KEY
    }

    private String roleArn;
    private String accessKey;
    private String secretKey;
    private String region;
    private String endpointUrl;
    private boolean signPayload;
    private boolean https = true;
    private boolean pathStyleAccess = true;

    private String bucket;
    private String folder;
    private String workFolder;

    private SecurityMode securityMode;
    private int refreshClientInSeconds = 10 * 60; // 10 minutes default

    private boolean enabled = true;

    public static S3Config forRoleArn(String roleArn, String region) {

        return new S3Config().setSecurityMode(SecurityMode.ROLE_ARN).setRoleArn(roleArn).setRegion(region);
    }

    public static S3Config forAccessKeys(String accessKey, String secretKey, String region) {

        return new S3Config().setSecurityMode(SecurityMode.ACCESS_KEY).setAccessKey(accessKey).setSecretKey(secretKey).setRegion(region);
    }

    public String getRoleArn() {
        return roleArn;
    }

    public S3Config setRoleArn(String roleArn) {
        this.roleArn = roleArn;
        return this;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public S3Config setAccessKey(String accessKey) {
        this.accessKey = accessKey;
        return this;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public S3Config setSecretKey(String secretKey) {
        this.secretKey = secretKey;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public S3Config setRegion(String region) {
        this.region = region;
        return this;
    }

    public SecurityMode getSecurityMode() {
        return securityMode;
    }

    public S3Config setSecurityMode(SecurityMode securityMode) {
        this.securityMode = securityMode;
        return this;
    }

    public int getRefreshClientInSeconds() {
        return refreshClientInSeconds;
    }

    public S3Config setRefreshClientInSeconds(int refreshClientInSeconds) {
        this.refreshClientInSeconds = refreshClientInSeconds;
        return this;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public S3Config setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
        return this;
    }

    public String getBucket() {
        return bucket;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public boolean isSignPayload() {
        return signPayload;
    }

    public S3Config setSignPayload(boolean signPayload) {
        this.signPayload = signPayload;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public S3Config setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isArn() {
        return (securityMode == SecurityMode.ROLE_ARN);
    }

    public boolean isHttps() {
        return https;
    }

    public S3Config setHttps(boolean https) {
        this.https = https;
        return this;
    }

    public boolean isPathStyleAccess() {
        return pathStyleAccess;
    }

    public S3Config setPathStyleAccess(boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
        return this;
    }

    public String getWorkFolder() {
        return workFolder;
    }

    public S3Config setWorkFolder(String workFolder) {
        this.workFolder = workFolder;
        return this;
    }

    @Override
    public String toString() {
        return "S3Config{" +
                super.toString() +
                "roleArn='" + roleArn + '\'' +
                ", accessKey='" + accessKey + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", region='" + region + '\'' +
                ", endpointUrl='" + endpointUrl + '\'' +
                ", signPayload=" + signPayload +
                ", https=" + https +
                ", pathStyleAccess=" + pathStyleAccess +
                ", bucket='" + bucket + '\'' +
                ", folder='" + folder + '\'' +
                ", securityMode=" + securityMode +
                ", refreshClientInSeconds=" + refreshClientInSeconds +
                ", enabled=" + enabled +
                '}';
    }
}
