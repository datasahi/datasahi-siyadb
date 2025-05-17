package datasahi.siyadb.duckdb;

import datasahi.siyadb.common.db.DatabaseConfig;
import datasahi.siyadb.common.db.DatabaseService;
import datasahi.siyadb.config.ConfigService;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DuckdbService {

    private static final Logger Logger = LoggerFactory.getLogger(DuckdbService.class);

    private final ConfigService configService;
    private DatabaseService dbService;

    public DuckdbService(ConfigService configService) {
        this.configService = configService;
    }

    @PostConstruct
    public void init() {
        String dataDir = configService.getDataDir();
        Logger.info("DuckDB data directory: {}", dataDir);

        DatabaseConfig config = new DatabaseConfig()
                .setDriverClass("org.duckdb.DuckDBDriver")
                .setUrl("jdbc:duckdb:" + dataDir + "/siyadb.duckdb")
                .setMaxPoolSize(5);

        this.dbService = new DatabaseService(config);
    }

    public DatabaseService getDbService() {
        return dbService;
    }
}
