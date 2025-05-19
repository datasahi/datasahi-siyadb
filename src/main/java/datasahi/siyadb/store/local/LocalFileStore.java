package datasahi.siyadb.store.local;

import datasahi.siyadb.store.FileStore;
import datasahi.siyadb.store.FileTransferRequest;
import datasahi.siyadb.store.FileTransferResponse;
import datasahi.siyadb.store.StoreConfig;

public class LocalFileStore implements FileStore {

    private final LocalStoreConfig config;

    public LocalFileStore(LocalStoreConfig config) {
        this.config = config;
    }

    @Override
    public StoreConfig getConfig() {
        return config;
    }

    @Override
    public FileTransferResponse download(FileTransferRequest request) {
        return null;
    }
}
