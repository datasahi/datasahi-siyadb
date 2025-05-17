package datasahi.siyadb.store;

public interface FileStore {

    String getId();

    FileTransferResponse download(FileTransferRequest request);

    public interface FileListProcessor {
        boolean processList(FileListResponse listResponse);
    }

}
