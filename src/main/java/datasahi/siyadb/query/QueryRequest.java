package datasahi.siyadb.query;

import datasahi.siyadb.load.FileKey;
import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable.Serializable
@Serdeable.Deserializable
public class QueryRequest {

    private String datasource;
    private String bucket;
    private List<String> filepaths;
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

    public List<String> getFilepaths() {
        return filepaths;
    }

    public QueryRequest setFilepaths(List<String> filepaths) {
        this.filepaths = filepaths;
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

    public List<FileKey> getFileKeys() {
        return filepaths.stream().map(filepath -> new FileKey(datasource, bucket, filepath)).toList();
    }

    @Override
    public String toString() {
        return "QueryRequest{" +
                "datasource='" + datasource + '\'' +
                ", bucket='" + bucket + '\'' +
                ", filepaths='" + filepaths + '\'' +
                ", filetype='" + filetype + '\'' +
                ", query='" + query + '\'' +
                '}';
    }
}
