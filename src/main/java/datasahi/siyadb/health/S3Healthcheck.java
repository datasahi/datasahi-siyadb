package datasahi.siyadb.health;

import datasahi.siyadb.store.FileTransferRequest;
import datasahi.siyadb.store.FileTransferResponse;
import datasahi.siyadb.store.s3.S3FileStore;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class S3Healthcheck implements Healthcheck {

    private static final Logger log = LoggerFactory.getLogger(S3Healthcheck.class);
    private final S3FileStore s3FileStore;
    private final String workFolder;

    public S3Healthcheck(S3FileStore s3FileStore, String workFolder) {
        this.s3FileStore = s3FileStore;
        this.workFolder = workFolder;
    }

    @Override
    public HealthResponse check() {

        String filepath = s3FileStore.getS3Config().getTestFile();
        FileTransferRequest request = new FileTransferRequest().setTargetPath(workFolder + filepath)
                .setSourcePath(filepath);
        String id = s3FileStore.getConfig().getId();
        try {
            FileTransferResponse response = s3FileStore.download(request);
            if (response.isExists()) {
                log.info("S3 filestore success :: {}", s3FileStore.getConfig());
                return new HealthResponse().setDataserverId(id).setHealthy(true);
            } else {
                log.error("Error connecting to S3, unexpected response");
                return new HealthResponse().setDataserverId(id).setHealthy(false);
            }
        } catch (Exception e) {
            log.error("Error connecting to S3 filestore", e);
            return new HealthResponse().setDataserverId(id).setHealthy(false).setMessage(e.getMessage());
        }
    }
}

