package datasahi.siyadb.store;

public class FileTransferRequest {

    private String targetPath; //path also include filename ie key
    private String sourcePath;  //path include dir and filename

    public String getTargetPath() {
        return targetPath;
    }

    public FileTransferRequest setTargetPath(String targetPath) {
        this.targetPath = targetPath;
        return this;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public FileTransferRequest setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
        return this;
    }

    @Override
    public String toString() {
        return "FileTransferRequest{" +
                "targetPath='" + targetPath + '\'' +
                ", sourcePath='" + sourcePath + '\'' +
                '}';
    }
}
