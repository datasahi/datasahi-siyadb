package datasahi.siyadb.system;

import datasahi.siyadb.load.DataLoadService;
import datasahi.siyadb.query.QueryAuditService;
import datasahi.siyadb.store.StoreRegistry;
import jakarta.inject.Singleton;

@Singleton
public class SystemInfoService {

    private final DataLoadService dataLoadService;
    private final StoreRegistry storeRegistry;
    private final QueryAuditService queryAuditService;

    public SystemInfoService(DataLoadService dataLoadService, StoreRegistry storeRegistry,
                             QueryAuditService queryAuditService) {
        this.dataLoadService = dataLoadService;
        this.storeRegistry = storeRegistry;
        this.queryAuditService = queryAuditService;
    }

    public SystemInfo getSystemInfo() {
        return new SystemInfo().setFileStores(storeRegistry.getFileStores())
                .setFileStates(dataLoadService.getFileStates())
                .setQueryAudits(queryAuditService.getAuditRecords());
    }
}
