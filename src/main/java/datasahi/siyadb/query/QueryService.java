package datasahi.siyadb.query;

import datasahi.siyadb.common.api.ServiceResponse;
import datasahi.siyadb.common.db.DatabaseService;
import datasahi.siyadb.duckdb.DuckdbService;
import datasahi.siyadb.load.DataLoadService;
import datasahi.siyadb.load.FileKey;
import jakarta.inject.Singleton;

@Singleton
public class QueryService {

    private final DatabaseService databaseService;
    private final DataLoadService dataLoadService;

    public QueryService(DuckdbService duckdbService, DataLoadService dataLoadService) {
        this.databaseService = duckdbService.getDbService();
        this.dataLoadService = dataLoadService;
    }

    public ServiceResponse<QueryResponse> execute(QueryRequest request) {
        dataLoadService.checkAndLoadFile(request.getFileKey());
        QueryResponse response = this.databaseService.selectAsText(request.getQuery());
        ServiceResponse<QueryResponse> serviceResponse = new ServiceResponse<>().setData(response);
        return serviceResponse;
    }
}
