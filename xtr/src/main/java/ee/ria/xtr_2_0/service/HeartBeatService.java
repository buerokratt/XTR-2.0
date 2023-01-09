package ee.ria.xtr_2_0.service;

import ee.ria.xtr_2_0.conf.PackageInfoConfiguration;
import ee.ria.xtr_2_0.dto.HeartBeatInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HeartBeatService {

    private final ServerInfoService serverInfoService;

    private final PackageInfoConfiguration packageInfoConfiguration;

    public HeartBeatService(ServerInfoService serverInfoService, PackageInfoConfiguration packageInfoConfiguration) {
        this.serverInfoService = serverInfoService;
        this.packageInfoConfiguration = packageInfoConfiguration;
    }

    public HeartBeatInfo getData() {
        return HeartBeatInfo.builder()
                .appName(packageInfoConfiguration.getAppName())
                .packagingTime(packageInfoConfiguration.getPackagingTime())
                .version(packageInfoConfiguration.getVersion())
                .appStartTime(serverInfoService.getStartupTime())
                .serverTime(serverInfoService.getServerTime())
                .build();
    }

}
