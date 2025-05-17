package datasahi.siyadb.store.local;

public class LocalStoreConfig {

    private String id;
    private String folder;

    public String getId() {
        return id;
    }

    public LocalStoreConfig setId(String id) {
        this.id = id;
        return this;
    }

    public String getFolder() {
        return folder;
    }

    public LocalStoreConfig setFolder(String folder) {
        this.folder = folder;
        return this;
    }

    @Override
    public String toString() {
        return "LocalStoreConfig{" +
                "id='" + id + '\'' +
                ", folder='" + folder + '\'' +
                '}';
    }
}
