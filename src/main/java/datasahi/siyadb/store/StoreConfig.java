package datasahi.siyadb.store;

public class StoreConfig {

    private String id;
    private StoreType type;
    private int cachedMinutes = 15; // Default to 15 mins

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

    public int getCachedMinutes() {
        return cachedMinutes;
    }

    public StoreConfig setCachedMinutes(int cachedMinutes) {
        this.cachedMinutes = cachedMinutes;
        return this;
    }

    @Override
    public String toString() {
        return "StoreConfig{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", cachedMinutes=" + cachedMinutes +
                '}';
    }
}
