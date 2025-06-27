package datasahi.siyadb.store;

import com.google.gson.Gson;
import datasahi.siyadb.config.ConfigService;
import datasahi.siyadb.config.ServerConfiguration;
import datasahi.siyadb.store.local.LocalFileStore;
import datasahi.siyadb.store.local.LocalStoreConfig;
import datasahi.siyadb.store.s3.S3Config;
import datasahi.siyadb.store.s3.S3FileStore;
import io.micronaut.context.annotation.Context;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
@Context
public class StoreRegistry {

    private final ConfigService configService;
    private final ServerConfiguration serverConfiguration;

    private Map<String, FileStore> fileStores = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();


    public StoreRegistry(ConfigService configService, ServerConfiguration serverConfiguration) {
        this.configService = configService;
        this.serverConfiguration = serverConfiguration;
    }

    @PostConstruct
    public void init() {
        serverConfiguration.getStores().forEach(this::register);
    }

    public void register(FileStore store) {
        fileStores.put(store.getConfig().getId(), store);
    }

    public void register(JSONObject dsJson) {

        String type = dsJson.getString("type");
        switch (StoreType.valueOf(type)) {
            case S3:
                S3Config s3Config = gson.fromJson(dsJson.toString(), S3Config.class);
                s3Config.setWorkFolder(configService.getWorkDir() + "/" + s3Config.getId());
                register(new S3FileStore(s3Config));
                break;
            case LOCAL:
                LocalStoreConfig config = gson.fromJson(dsJson.toString(), LocalStoreConfig.class);
                register(new LocalFileStore(config));
                break;
            default:
                throw new IllegalArgumentException("Unsupported filestore type: " + type);
        }
    }

    public FileStore get(String id) {
        return fileStores.get(id);
    }

    public List<FileStore> getFileStores() {
        return List.copyOf(fileStores.values());
    }
}
