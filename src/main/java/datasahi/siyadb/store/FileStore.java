package datasahi.siyadb.store;

public interface FileStore {

    StoreConfig getConfig();

    FileTransferResponse download(FileTransferRequest request);

    FileTransferResponse upload(FileTransferRequest transferRequest);

    FileListResponse listFiles(String folder);

    interface FileListProcessor {
        boolean processList(FileListResponse listResponse);
    }

}
