package datasahi.siyadb.store;

public class FileTransferResponse {

    private FileTransferRequest request;
    private FileInfo fileInfo;
    private long timeInMillis;
    private boolean exists;

    public FileTransferRequest getRequest() {
        return request;
    }


    public void setRequest(FileTransferRequest request) {
        this.request = request;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public boolean isExists() {
        return exists;
    }

    public FileTransferResponse setExists(boolean exists) {
        this.exists = exists;
        return this;
    }

    @Override
    public String toString() {
        return "FileTransferResponse{" +
                "request=" + request +
                ", fileInfo=" + fileInfo +
                ", timeInMillis=" + timeInMillis +
                '}';
    }
}
