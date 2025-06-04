package datasahi.siyadb.store;

public interface FileStore {

    StoreConfig getConfig();

    FileTransferResponse download(FileTransferRequest request);

    interface FileListProcessor {
        boolean processList(FileListResponse listResponse);
    }

}
