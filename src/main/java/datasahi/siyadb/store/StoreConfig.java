package datasahi.siyadb.store;

public class StoreConfig {

    private String id;
    private StoreType type;

    public String getId() {
        return id;
    }

    public StoreConfig setId(String id) {
        this.id = id;
        return this;
    }

    public StoreType getType() {
        return type;
    }

    public StoreConfig setType(StoreType type) {
        this.type = type;
        return this;
    }

    @Override
    public String toString() {
        return "StoreConfig{" +
                "id='" + id + '\'' +
                ", type=" + type +
                '}';
    }
}
