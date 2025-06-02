package datasahi.siyadb.system;

import datasahi.siyadb.load.FileState;
import datasahi.siyadb.query.QueryAudit;
import datasahi.siyadb.store.FileStore;

import java.util.Collections;
import java.util.List;

public class SystemInfo {

    private List<FileStore> fileStores = Collections.emptyList();
    private List<FileState> fileStates;
    private List<QueryAudit> queryAudits;

    public List<FileStore> getFileStores() {
        return fileStores;
    }

    public SystemInfo setFileStores(List<FileStore> fileStores) {
//        this.fileStores = fileStores;
        return this;
    }

    public List<FileState> getFileStates() {
        return fileStates;
    }

    public SystemInfo setFileStates(List<FileState> fileStates) {
        this.fileStates = fileStates;
        return this;
    }

    public List<QueryAudit> getQueryAudits() {
        return queryAudits;
    }

    public SystemInfo setQueryAudits(List<QueryAudit> queryAudits) {
        this.queryAudits = queryAudits;
        return this;
    }

    @Override
    public String toString() {
        return "SystemInfo{" +
                "fileStores=" + fileStores +
                ", fileStates=" + fileStates +
                ", queryAudits=" + queryAudits +
                '}';
    }
}
