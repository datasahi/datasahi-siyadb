package datasahi.siyadb.query;

import datasahi.siyadb.load.FileKey;

public class QueryRequest {

    private String datasource;
    private String bucket;
    private String filepath;
    private String filetype;
    private String query;

    public String getDatasource() {
        return datasource;
    }

    public QueryRequest setDatasource(String datasource) {
        this.datasource = datasource;
        return this;
    }

    public String getBucket() {
        return bucket;
    }

    public QueryRequest setBucket(String bucket) {
        this.bucket = bucket;
        return this;
    }

    public String getFilepath() {
        return filepath;
    }

    public QueryRequest setFilepath(String filepath) {
        this.filepath = filepath;
        return this;
    }

    public String getFiletype() {
        return filetype;
    }

    public QueryRequest setFiletype(String filetype) {
        this.filetype = filetype;
        return this;
    }

    public String getQuery() {
        return query;
    }

    public QueryRequest setQuery(String query) {
        this.query = query;
        return this;
    }

    public FileKey getFileKey() {
        return new FileKey(datasource, bucket, filepath);
    }

    @Override
    public String toString() {
        return "QueryRequest{" +
                "datasource='" + datasource + '\'' +
                ", bucket='" + bucket + '\'' +
                ", filepath='" + filepath + '\'' +
                ", filetype='" + filetype + '\'' +
                ", query='" + query + '\'' +
                '}';
    }
}
