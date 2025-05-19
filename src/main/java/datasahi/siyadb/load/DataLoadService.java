package datasahi.siyadb.load;

import datasahi.siyadb.common.db.DatabaseService;
import datasahi.siyadb.config.ConfigService;
import datasahi.siyadb.duckdb.DuckdbService;
import datasahi.siyadb.store.FileTransferRequest;
import datasahi.siyadb.store.FileTransferResponse;
import datasahi.siyadb.store.StoreRegistry;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class DataLoadService {

    private final DatabaseService databaseService;
    private final StoreRegistry storeRegistry;
    private final ConfigService configService;

    private final Map<FileKey, FileState> fileStates = new ConcurrentHashMap<>();

    public DataLoadService(DuckdbService duckdbService, StoreRegistry storeRegistry,
                           ConfigService configService) {
        this.databaseService = duckdbService.getDbService();
        this.storeRegistry = storeRegistry;
        this.configService = configService;
    }

    public boolean checkAndLoadFile(FileKey fileKey) {

        FileState fileState = fileStates.get(fileKey);
        if (fileState == null) {
            synchronized (this) {
                fileState = fileStates.get(fileKey);
                if (fileState != null) {
                    return true; // Another thread already loaded the file
                }
                fileState = new FileState(fileKey);
                fileStates.put(fileKey, fileState);
            }
        }

        fileState = fileStates.get(fileKey);
        if (fileState.isLoaded()) return true;

        synchronized (fileState.getFileKey()) {
            if (fileState.isLoaded()) return true;
            String tableName = loadFile(fileKey);
            fileState.setTableName(tableName).setLastAccessMillis(System.currentTimeMillis()).setLoaded(true);
        }

        return true;
    }

    private String loadFile(FileKey fileKey) {

        String sourcePath = fileKey.getSourcePath();
        String targetPath = configService.getWorkDir() + "/" + sourcePath;
        FileTransferRequest request = new FileTransferRequest().setSourcePath(sourcePath)
                .setTargetPath(targetPath);
        FileTransferResponse response = storeRegistry.get(fileKey.getDatastore()).download(request);
        if (response.isExists()) {
            String tableName = fileKey.getTableName();
            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " AS SELECT * FROM read_csv_auto('" +
                    targetPath + "', HEADER = true);";
            databaseService.executeUpdateSql(sql);
            databaseService.executeUpdateSql("ANALYZE " + tableName);
            return tableName;
            //todo create indexes
        } else {
            throw new RuntimeException("Unable to load file from :: " + sourcePath);
        }
    }
}
