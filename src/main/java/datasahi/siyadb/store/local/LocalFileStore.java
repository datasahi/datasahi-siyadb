package datasahi.siyadb.store.local;

import datasahi.siyadb.store.*;

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

    @Override
    public FileTransferResponse upload(FileTransferRequest transferRequest) {
        return null;
    }

    @Override
    public FileListResponse listFiles(String folder) {
        return null;
    }
}
