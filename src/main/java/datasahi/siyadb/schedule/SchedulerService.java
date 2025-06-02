package datasahi.siyadb.schedule;

import datasahi.siyadb.config.ConfigService;
import datasahi.siyadb.load.DataLoadService;
import io.micronaut.context.annotation.Context;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;

import java.util.Timer;
import java.util.TimerTask;

@Context
@Singleton
public class SchedulerService {

    private final ConfigService configService;
    private final DataLoadService dataLoadService;

    public SchedulerService(ConfigService configService, DataLoadService dataLoadService) {
        this.configService = configService;
        this.dataLoadService = dataLoadService;
    }

    @PostConstruct
    public void init() {
        startCleanupTask();
    }

    private void startCleanupTask() {
        long delay = configService.getCleanupSeconds() * 1000L;
        new Timer("Cleanup Scheduler").schedule(new TimerTask() {
            @Override
            public void run() {
                dataLoadService.cleanup();
            }
        }, delay, delay); // Schedule cleanup every 5 minutes
    }
}
