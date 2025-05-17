package datasahi.siyadb.store.s3;

public class S3Config {

    public enum Type {
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

    private Type type;
    private int refreshClientInSeconds = 10 * 60; // 10 minutes default

    private boolean enabled = true;

    public static S3Config forRoleArn(String roleArn, String region) {

        return new S3Config().setType(Type.ROLE_ARN).setRoleArn(roleArn).setRegion(region);
    }

    public static S3Config forAccessKeys(String accessKey, String secretKey, String region) {

        return new S3Config().setType(Type.ACCESS_KEY).setAccessKey(accessKey).setSecretKey(secretKey).setRegion(region);
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

    public Type getType() {
        return type;
    }

    public S3Config setType(Type type) {
        this.type = type;
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

    public String getWorkFolder() {
        return workFolder;
    }

    public void setWorkFolder(String workFolder) {
        this.workFolder = workFolder;
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
        return (type == Type.ROLE_ARN);
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

    @Override
    public String toString() {
        return "S3Config{" +
                "roleArn='" + roleArn + '\'' +
                ", region='" + region + '\'' +
                ", endpointUrl='" + endpointUrl + '\'' +
                ", signPayload=" + signPayload +
                ", https=" + https +
                ", pathStyleAccess=" + pathStyleAccess +
                ", bucket='" + bucket + '\'' +
                ", folder='" + folder + '\'' +
                ", workFolder='" + workFolder + '\'' +
                ", type=" + type +
                ", refreshClientInSeconds=" + refreshClientInSeconds +
                ", enabled=" + enabled +
                '}';
    }
}
