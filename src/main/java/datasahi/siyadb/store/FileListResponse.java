package datasahi.siyadb.store;

import java.util.ArrayList;
import java.util.List;

public class FileListResponse {

    private final List<FileInfo> files;
    private long responseInMillis;
    private String nextMarker;
    private boolean moreFiles = false;

    public FileListResponse() {
        this.files = new ArrayList<>();
    }

    public boolean isEmpty() {
        return files.isEmpty();
    }

    public void addFile(FileInfo fileInfo) {
        files.add(fileInfo);
    }

    public List<FileInfo> getFiles() {
        return files;
    }

    public long getResponseInMillis() {
        return responseInMillis;
    }

    public FileListResponse setResponseInMillis(long responseInMillis) {
        this.responseInMillis = responseInMillis;
        return this;
    }

    public String getNextMarker() {
        return nextMarker;
    }

    public FileListResponse setNextMarker(String nextMarker) {
        this.nextMarker = nextMarker;
        return this;
    }

    public boolean hasMoreFiles() {
        return moreFiles;
    }

    public FileListResponse setMoreFiles(boolean moreFiles) {
        this.moreFiles = moreFiles;
        return this;
    }

    public String prepareNextMarker() {
        if (files.isEmpty()) return null;

        FileInfo lastfile = files.get(files.size() - 1);
        String fullpath = lastfile.getFullFilePath();
        int index = fullpath.indexOf('/');
        return fullpath.substring(index + 1);
    }

    @Override
    public String toString() {
        return "FileListResponse{" +
                "files=" + files +
                ", responseInMillis=" + responseInMillis +
                ", nextMarker='" + nextMarker + '\'' +
                ", moreFiles=" + moreFiles +
                '}';
    }
}
