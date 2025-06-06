package datasahi.siyadb.store;

import java.util.List;

public class Dataset {

    private String id;
    private String datastore;
    private String name;
    private String filePattern;
    private List<String> indices;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDatastore() {
        return datastore;
    }

    public void setDatastore(String datastore) {
        this.datastore = datastore;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFilePattern() {
        return filePattern;
    }

    public void setFilePattern(String filePattern) {
        this.filePattern = filePattern;
    }

    public List<String> getIndices() {
        return indices;
    }

    public void setIndices(List<String> indices) {
        this.indices = indices;
    }

    // toString method for object representation
    @Override
    public String toString() {
        return "EcomOrder{" +
                "id='" + id + '\'' +
                ", datastore='" + datastore + '\'' +
                ", name='" + name + '\'' +
                ", filePattern='" + filePattern + '\'' +
                ", indices=" + indices +
                '}';
    }
}