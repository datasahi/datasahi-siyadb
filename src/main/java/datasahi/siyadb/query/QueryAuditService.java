package datasahi.siyadb.query;

import datasahi.siyadb.common.api.ServiceResponse;
import datasahi.siyadb.common.util.CircularRecordBuffer;
import jakarta.inject.Singleton;
import org.json.JSONObject;

import java.util.List;

@Singleton
public class QueryAuditService {

    private final CircularRecordBuffer<QueryAudit> records = new CircularRecordBuffer<>(100);

    public void add(QueryAudit queryAudit) {
        records.add(queryAudit);
    }

    public List<QueryAudit> getAuditRecords() {
        return records.getRecords();
    }

    public void audit(QueryRequest request, QueryResponse response, ServiceResponse<JSONObject> serviceResponse) {
        QueryAudit queryAudit = new QueryAudit()
                .setDatasource(request.getDatastore())
                .setQuery(request.getQuery())
                .setRecordCount(response != null ? response.getCount() : 0)
                .setSuccess(serviceResponse.isSuccess())
                .setMillis(serviceResponse.getMillis());
        add(queryAudit);
    }
}
