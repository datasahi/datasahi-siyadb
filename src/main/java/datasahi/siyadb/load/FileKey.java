package datasahi.siyadb.load;

import io.micronaut.serde.annotation.Serdeable;

import java.util.Objects;

@Serdeable.Serializable
public class FileKey {

    private final String datastore;
    private final String bucket;
    private final String filepath;

    public FileKey(String datastore, String bucket, String filepath) {
        this.datastore = datastore;
        this.bucket = bucket;
        this.filepath = filepath;
    }

    public String getDatastore() {
        return datastore;
    }

    public String getBucket() {
        return bucket;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getSourcePath() {
        return bucket + "/" + filepath;
    }

    public String getTableName() {
        String tableName = datastore + "_" + bucket + "_" + filepath;
        return tableName.replaceAll("[^a-zA-Z0-9]", "_");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileKey fileKey = (FileKey) o;
        return Objects.equals(datastore, fileKey.datastore) && Objects.equals(bucket, fileKey.bucket) && Objects.equals(filepath, fileKey.filepath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(datastore, bucket, filepath);
    }

    @Override
    public String toString() {
        return "FileKey{" +
                "datasource='" + datastore + '\'' +
                ", bucket='" + bucket + '\'' +
                ", filepath='" + filepath + '\'' +
                '}';
    }
}
