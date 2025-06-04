package datasahi.siyadb.query;

public class QueryAudit {

    private String datasource;
    private String query;
    private int recordCount;
    private boolean success;
    private long millis;
    private String message;

    public String getDatasource() {
        return datasource;
    }

    public QueryAudit setDatasource(String datasource) {
        this.datasource = datasource;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public QueryAudit setQuery(String query) {
        this.query = query;
        return this;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public QueryAudit setRecordCount(int recordCount) {
        this.recordCount = recordCount;
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public QueryAudit setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    public long getMillis() {
        return millis;
    }

    public QueryAudit setMillis(long millis) {
        this.millis = millis;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public QueryAudit setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        return "QueryAudit{" +
                "datasource='" + datasource + '\'' +
                ", query='" + query + '\'' +
                ", recordCount=" + recordCount +
                ", success=" + success +
                ", message=" + message +
                ", millis=" + millis +
                '}';
    }
}
