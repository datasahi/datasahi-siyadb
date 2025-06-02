package datasahi.siyadb.api;

import datasahi.siyadb.common.api.ServiceResponse;
import datasahi.siyadb.health.HealthCheckService;
import datasahi.siyadb.health.HealthSummary;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller("/siyadb/health")
public class HealthController {

    private final HealthCheckService healthCheckService;

    public HealthController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    @Produces(MediaType.TEXT_PLAIN)
    @Get
    public String isHealthy() {
        return "true";
    }

    @Produces(MediaType.TEXT_JSON)
    @Get("/check")
    public String doHealthCheck() {
        long start = System.currentTimeMillis();
        HealthSummary healthSummary = healthCheckService.performHealthCheck();
        long millis = System.currentTimeMillis() - start;
        return new ServiceResponse<HealthSummary>().setSuccess(true).setData(healthSummary).setMillis(millis).toJsonString();
    }
}
