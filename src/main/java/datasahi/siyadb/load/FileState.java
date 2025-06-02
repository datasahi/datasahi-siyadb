package datasahi.siyadb.load;

import java.util.Objects;

public class FileState {

    private final FileKey fileKey;
    private String localPath;
    private String tableName;
    private long lastAccessMillis;
    private volatile boolean loaded;

    private int cachedMillis = 15 * 60 * 1000; // Default to 15 minutes

    public FileState(FileKey fileKey) {
        this.fileKey = fileKey;
    }

    public FileKey getFileKey() {
        return fileKey;
    }

    public String getLocalPath() {
        return localPath;
    }

    public FileState setLocalPath(String localPath) {
        this.localPath = localPath;
        return this;
    }

    public long getLastAccessMillis() {
        return lastAccessMillis;
    }

    public FileState setLastAccessMillis(long lastAccessMillis) {
        this.lastAccessMillis = lastAccessMillis;
        return this;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public FileState setLoaded(boolean loaded) {
        this.loaded = loaded;
        return this;
    }

    public String getTableName() {
        return tableName;
    }

    public FileState setTableName(String tableName) {
        this.tableName = tableName;
        return this;
    }

    public int getCachedMillis() {
        return cachedMillis;
    }

    public FileState setCachedMillis(int cachedMillis) {
        this.cachedMillis = cachedMillis;
        return this;
    }

    public boolean isExpired() {
        return (System.currentTimeMillis() - lastAccessMillis) > cachedMillis;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileState fileState = (FileState) o;
        return Objects.equals(fileKey, fileState.fileKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fileKey);
    }

    @Override
    public String toString() {
        return "FileState{" +
                "fileKey=" + fileKey +
                ", localPath='" + localPath + '\'' +
                ", tableName='" + tableName + '\'' +
                ", lastAccessMillis=" + lastAccessMillis +
                ", loaded=" + loaded +
                ", cachedMillis=" + cachedMillis +
                '}';
    }
}
