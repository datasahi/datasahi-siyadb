package datasahi.siyadb.query;

import datasahi.siyadb.common.api.ServiceResponse;
import datasahi.siyadb.common.db.DatabaseService;
import datasahi.siyadb.common.db.OutputFormat;
import datasahi.siyadb.duckdb.DuckdbService;
import datasahi.siyadb.load.DataLoadService;
import jakarta.inject.Singleton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@Singleton
public class QueryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryService.class);

    private final DatabaseService databaseService;
    private final DataLoadService dataLoadService;
    private final QueryAuditService queryAuditService;

    public QueryService(DuckdbService duckdbService, DataLoadService dataLoadService, QueryAuditService queryAuditService) {
        this.databaseService = duckdbService.getDbService();
        this.dataLoadService = dataLoadService;
        this.queryAuditService = queryAuditService;
    }

    public ServiceResponse<JSONObject> execute(QueryRequest request) {
        long start = System.currentTimeMillis();

        QueryResponse response = null;
        ServiceResponse<JSONObject> serviceResponse = null;
        try {
            request.getFileKeys().forEach(dataLoadService::checkAndLoad);
            response = databaseService.selectAsText(request.getQuery(), Collections.emptyMap(), OutputFormat.JSON);
            JSONObject json = new JSONObject();
            json.put("id", response.getId());
            json.put("count", response.getCount());
            json.put("records", new JSONArray(response.getRecords()));
            serviceResponse = new ServiceResponse<>().setData(json).setSuccess(true).setMillis(System.currentTimeMillis() - start);
        } catch (Exception e) {
            serviceResponse =
                    new ServiceResponse<>().setSuccess(false).setMessage(e.getMessage()).setMillis(System.currentTimeMillis() - start);
            LOGGER.error("Error executing query: {}", request, e);
        }
        queryAuditService.audit(request, response, serviceResponse);
        return serviceResponse;
    }
}
