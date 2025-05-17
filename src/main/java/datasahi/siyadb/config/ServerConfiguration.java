package datasahi.siyadb.config;

import datasahi.siyadb.common.file.FileUtil;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class ServerConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ServerConfiguration.class);
    private final ConfigService configService;

    private final List<JSONObject> stores = new ArrayList<>();

    public ServerConfiguration(ConfigService configService) {
        this.configService = configService;
    }

    @PostConstruct
    public void load() {
        configService.getConfigPaths().forEach(p -> loadConfig(p));
        LOG.info("Loaded {} data servers", stores.size());
    }

    public List<JSONObject> getStores() {
        return stores;
    }

    private void loadConfig(String path) {
        JSONObject jo = new FileUtil().readJsonFile(path);
        addJsonObjects(jo, stores, "dataservers");
    }

    private void addJsonObjects(JSONObject jo, List<JSONObject> list, String key) {
        JSONArray dsa = jo.getJSONArray(key);
        for (int i = 0; i < dsa.length(); i++) {
            list.add(dsa.getJSONObject(i));
        }
    }
}
