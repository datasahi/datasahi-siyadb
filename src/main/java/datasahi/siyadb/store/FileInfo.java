package datasahi.siyadb.store;

public class FileInfo {

    private String path;              // path include bucket-name and folder name(path in S3 ex in bucket/folder1/key --> path is 'bucket/folder1')
    private String filename;         // only filename starting from last '/' (ex- in bucket/folder1/key --> filename is 'key')
    private long sizeInBytes;

    public String getPath() {
        return path;
    }

    public FileInfo setPath(String path) {
        this.path = path;
        return this;
    }

    public String getFilename() {
        return filename;
    }

    public FileInfo setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public FileInfo setSizeInBytes(long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
        return this;
    }

    public String getFullFilePath() {
        return path + "/" + filename;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "path='" + path + '\'' +
                ", filename='" + filename + '\'' +
                ", sizeInBytes=" + sizeInBytes +
                '}';
    }
}
