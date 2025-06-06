package datasahi.siyadb.load;

import datasahi.siyadb.common.db.DatabaseService;
import datasahi.siyadb.config.ConfigService;
import datasahi.siyadb.duckdb.DuckdbService;
import datasahi.siyadb.store.DatasetRegistry;
import datasahi.siyadb.store.FileTransferRequest;
import datasahi.siyadb.store.FileTransferResponse;
import datasahi.siyadb.store.StoreRegistry;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class DataLoadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataLoadService.class);

    private final DatabaseService databaseService;
    private final StoreRegistry storeRegistry;
    private final DatasetRegistry datasetRegistry;
    private final ConfigService configService;

    private final Map<FileKey, FileState> fileStates = new ConcurrentHashMap<>();

    public DataLoadService(DuckdbService duckdbService, StoreRegistry storeRegistry, DatasetRegistry datasetRegistry,
                           ConfigService configService) {
        this.databaseService = duckdbService.getDbService();
        this.storeRegistry = storeRegistry;
        this.datasetRegistry = datasetRegistry;
        this.configService = configService;
    }

    public boolean checkAndLoad(FileKey fileKey) {

        FileState fileState = fileStates.get(fileKey);
        if (fileState == null) {
            synchronized (this) {
                fileState = fileStates.get(fileKey);
                if (fileState != null) {
                    return true; // Another thread already loaded the file
                }
                fileState = new FileState(fileKey);
                fileState.setCachedMillis(storeRegistry.get(fileKey.getDatastore()).getConfig().getCachedMinutes() * 60 * 1000);
                fileStates.put(fileKey, fileState);
                LOGGER.info("File loaded :: " + fileState);
            }
        }

        fileState = fileStates.get(fileKey);
        if (fileState.isLoaded()) return true;

        synchronized (fileState.getFileKey()) {
            if (fileState.isLoaded()) return true;
            String tableName = load(fileState);
            fileState.setTableName(tableName).setLastAccessMillis(System.currentTimeMillis()).setLoaded(true);
        }

        return true;
    }

    private String load(FileState fileState) {

        FileKey fileKey = fileState.getFileKey();
        String sourcePath = fileKey.getSourcePath();
        String targetPath = configService.getWorkDir() + "/" + sourcePath;
        fileState.setLocalPath(targetPath);
        FileTransferRequest request = new FileTransferRequest().setSourcePath(sourcePath)
                .setTargetPath(targetPath);
        FileTransferResponse response = storeRegistry.get(fileKey.getDatastore()).download(request);
        if (response.isExists()) {
            String tableName = fileKey.getTableName();
            createTable(tableName, targetPath);
            createIndices(tableName, fileKey);
            return tableName;
        } else {
            throw new RuntimeException("Unable to load file from :: " + sourcePath);
        }
    }

    private void createIndices(String tableName, FileKey fileKey) {
        List<String> indices = datasetRegistry.getIndices(fileKey);
        if (indices.isEmpty()) return;

        int counter = 1;
        for (String index : indices) {
            String indexName = tableName + "_" + counter++;
            String createIndexSql = "CREATE INDEX IF NOT EXISTS " + indexName + " ON " + tableName + " (" + index + ");";
            databaseService.executeUpdateSql(createIndexSql);
        }
    }

    private void createTable(String tableName, String targetPath) {
        String dropSql = "DROP TABLE IF EXISTS " + tableName + ";";
        databaseService.executeUpdateSql(dropSql);
        String createSql = "CREATE TABLE IF NOT EXISTS " + tableName + " AS SELECT * FROM read_csv_auto('" +
                targetPath + "', HEADER = true);";
        databaseService.executeUpdateSql(createSql);
        databaseService.executeUpdateSql("ANALYZE " + tableName);
    }

    public void cleanup() {
        for (FileState fs : fileStates.values()) {
            if (fs.isExpired()) {
                unload(fs.getFileKey());
            }
        }
    }

    public void unload(FileKey fileKey) {

        FileState fileState = fileStates.get(fileKey);
        if (fileState == null) return;
        if (!fileState.isLoaded()) return;

        LOGGER.info("Unloading file: {}", fileState);

        synchronized (this) {
            fileStates.remove(fileKey);
        }

        int indicesCount = datasetRegistry.getIndices(fileKey).size();

        synchronized (fileState.getFileKey()) {

            String tableName = fileState.getTableName();

            databaseService.executeUpdateSql("DROP TABLE IF EXISTS " + tableName);
            for (int counter = 1; counter <= indicesCount; counter++) {
                String indexName = tableName + "_" + counter;
                String createIndexSql = "DROP INDEX IF EXISTS " + indexName + ";";
                databaseService.executeUpdateSql(createIndexSql);
            }

            try {
                Files.deleteIfExists(Paths.get(fileState.getLocalPath()));
            } catch (IOException e) {
                // Nothing to do
                LOGGER.warn("Failed to delete local file: {}", fileState.getLocalPath(), e);
            }
        }
    }

    public List<FileState> getFileStates() {
        return List.copyOf(fileStates.values());
    }
}
