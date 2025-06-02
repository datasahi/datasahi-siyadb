package datasahi.siyadb.health;

import datasahi.siyadb.config.ConfigService;
import datasahi.siyadb.store.FileStore;
import datasahi.siyadb.store.StoreRegistry;
import datasahi.siyadb.store.s3.S3FileStore;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HealthCheckService {

    private static final Logger log = LoggerFactory.getLogger(HealthCheckService.class);

    private final ConfigService configService;
    private final StoreRegistry storeRegistry;
    private final DuckdbHealthcheck duckdbHealthcheck;

    public HealthCheckService(ConfigService configService, StoreRegistry storeRegistry, DuckdbHealthcheck duckdbHealthcheck) {
        this.configService = configService;
        this.storeRegistry = storeRegistry;
        this.duckdbHealthcheck = duckdbHealthcheck;
    }

    public HealthSummary performHealthCheck() {
        HealthSummary healthSummary = new HealthSummary();
        healthSummary.add(duckdbHealthcheck.check());

        for (FileStore fs : storeRegistry.getFileStores()) {
            log.info("Performing health check for {}", fs.getConfig().getId());
            switch (fs.getConfig().getType()) {
                case S3:
                    healthSummary.add(new S3Healthcheck((S3FileStore) fs, configService.getWorkDir()).check());
                    break;
                default:
                    healthSummary.add(new HealthResponse().setDataserverId(fs.getConfig().getId()).setHealthy(false)
                            .setMessage("Health check not supported for filestore type: " + fs.getConfig().getType()));
            }
        }
        return healthSummary;
    }
}
