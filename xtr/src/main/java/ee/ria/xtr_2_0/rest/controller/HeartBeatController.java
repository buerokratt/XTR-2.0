package ee.ria.xtr_2_0.rest.controller;

import ee.ria.xtr_2_0.dto.HeartBeatInfo;
import ee.ria.xtr_2_0.service.HeartBeatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HeartBeatController {

    public static final String URL = "/healthz";

    private final HeartBeatService heartBeatService;

    public HeartBeatController(HeartBeatService heartBeatService) {
        this.heartBeatService = heartBeatService;
    }

    @RequestMapping(value = URL)
    public ResponseEntity<HeartBeatInfo> getData() {
        return ResponseEntity.ok().body(heartBeatService.getData());
    }

}
