package datasahi.siyadb.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import datasahi.siyadb.common.file.FileUtil;
import datasahi.siyadb.store.Dataset;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class ServerConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ServerConfiguration.class);
    private final ConfigService configService;

    private final List<JSONObject> stores = new ArrayList<>();
    private final List<Dataset> datasets = new ArrayList<>();

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

    public List<Dataset> getDatasets() {
        return datasets;
    }

    private void loadConfig(String path) {
        JSONObject jo = new FileUtil().readJsonFile(path);
        addJsonObjects(jo, stores, "datastores");
        if (jo.has("datasets")) {
            this.datasets.addAll(parseDatasetArray(jo.optJSONArray("datasets").toString()));
        }
    }

    private void addJsonObjects(JSONObject jo, List<JSONObject> list, String key) {
        if (!jo.has(key)) return;
        JSONArray dsa = jo.getJSONArray(key);
        for (int i = 0; i < dsa.length(); i++) {
            list.add(dsa.getJSONObject(i));
        }
    }

    private List<Dataset> parseDatasetArray(String jsonArray) {
        Gson gson = new Gson();
        Type datasetListType = new TypeToken<List<Dataset>>(){}.getType();
        return gson.fromJson(jsonArray, datasetListType);
    }
}
