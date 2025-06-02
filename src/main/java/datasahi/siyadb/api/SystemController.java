package datasahi.siyadb.api;

import datasahi.siyadb.common.api.ServiceResponse;
import datasahi.siyadb.health.HealthSummary;
import datasahi.siyadb.system.SystemInfo;
import datasahi.siyadb.system.SystemInfoService;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller("/siyadb/system")
public class SystemController {

    private final SystemInfoService systemInfoService;

    public SystemController(SystemInfoService systemInfoService) {
        this.systemInfoService = systemInfoService;
    }

    @Produces(MediaType.TEXT_JSON)
    @Get("/info")
    public String provideSystemInfo() {
        long start = System.currentTimeMillis();
        SystemInfo systemInfo = systemInfoService.getSystemInfo();
        long millis = System.currentTimeMillis() - start;
        return new ServiceResponse<HealthSummary>().setSuccess(true).setData(systemInfo).setMillis(millis).toJsonString();
    }
}
