package datasahi.siyadb.store.local;

import datasahi.siyadb.store.StoreConfig;

public class LocalStoreConfig extends StoreConfig {

    private String folder;

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
                super.toString() +
                "folder='" + folder + '\'' +
                '}';
    }
}
