package datasahi.siyadb.health;

import datasahi.siyadb.common.db.DatabaseService;
import datasahi.siyadb.common.db.OutputFormat;
import datasahi.siyadb.duckdb.DuckdbService;
import datasahi.siyadb.load.DataLoadService;
import datasahi.siyadb.query.QueryResponse;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@Singleton
public class DuckdbHealthcheck implements Healthcheck {

    private static final Logger log = LoggerFactory.getLogger(DuckdbHealthcheck.class);

    private final DatabaseService databaseService;

    public DuckdbHealthcheck(DuckdbService duckdbService) {
        this.databaseService = duckdbService.getDbService();
    }

    @Override
    public HealthResponse check() {

        String id = "duckdb " + databaseService.getConfig().getUrl();
        try {
            QueryResponse response = databaseService.selectAsText("select 1 as success", Collections.emptyMap(),
                    OutputFormat.JSON);
            if (response.getCount() == 1) {
                log.info("Duckdb connection success");
                return new HealthResponse().setDataserverId(id).setHealthy(true);
            } else {
                log.error("Error connecting to JDBC, unexpected response");
                return new HealthResponse().setDataserverId(id).setHealthy(false).setMessage("select 1 did not work");
            }
        } catch (Exception e) {
            log.error("Error connecting to local duckdb server", e);
            return new HealthResponse().setDataserverId(id).setHealthy(false).setMessage(e.getMessage());
        }
    }
}

