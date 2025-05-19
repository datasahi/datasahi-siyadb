package datasahi.siyadb.query;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class QueryResponse {

    private String id;
    private int count;
    private String records;

    public String getId() {
        return id;
    }

    public QueryResponse setId(String id) {
        this.id = id;
        return this;
    }

    public int getCount() {
        return count;
    }

    public QueryResponse setCount(int count) {
        this.count = count;
        return this;
    }

    public String getRecords() {
        return records;
    }

    public QueryResponse setRecords(String records) {
        this.records = records;
        return this;
    }

    @Override
    public String toString() {
        return "QueryResponse{" +
                "id='" + id + '\'' +
                ", count=" + count +
                ", records='" + records + '\'' +
                '}';
    }
}
