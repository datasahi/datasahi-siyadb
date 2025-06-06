package datasahi.siyadb.store;

import datasahi.siyadb.config.ServerConfiguration;
import datasahi.siyadb.load.FileKey;
import datasahi.siyadb.store.local.LocalFileStore;
import datasahi.siyadb.store.local.LocalStoreConfig;
import datasahi.siyadb.store.s3.S3Config;
import datasahi.siyadb.store.s3.S3FileStore;
import io.micronaut.context.annotation.Context;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Singleton
@Context
public class DatasetRegistry {

    private final ServerConfiguration serverConfiguration;

    private Map<String, List<Dataset>> datasetsMap = new ConcurrentHashMap<>();
    private Map<String, Pattern> patternMap = new ConcurrentHashMap<>();

    public DatasetRegistry(ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    @PostConstruct
    public void init() {
        serverConfiguration.getDatasets().forEach(this::register);
    }

    public void register(Dataset dataset) {
        datasetsMap.computeIfAbsent(dataset.getDatastore(), k -> new ArrayList<>()).add(dataset);
        if (dataset.getFilePattern() != null && !dataset.getFilePattern().isEmpty()) {
            patternMap.put(dataset.getFilePattern(), Pattern.compile(dataset.getFilePattern()));
        }
    }

    public List<String> getIndices(FileKey fileKey) {
        List<Dataset> datasets = datasetsMap.get(fileKey.getDatastore());
        if (datasets == null) {
            return List.of();
        }

        List<String> indices = new ArrayList<>();
        for (Dataset dataset : datasets) {
            if (dataset.getFilePattern() != null && patternMap.containsKey(dataset.getFilePattern())) {
                if (patternMap.get(dataset.getFilePattern()).matcher(fileKey.getSourcePath()).matches()) {
                    return dataset.getIndices();
                }
            }
        }
        return indices;

    }
}
