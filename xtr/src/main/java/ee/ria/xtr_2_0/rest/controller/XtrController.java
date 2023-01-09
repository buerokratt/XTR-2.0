package ee.ria.xtr_2_0.rest.controller;

import com.google.gson.Gson;
import ee.ria.xtr_2_0.model.XtrRequest;
import ee.ria.xtr_2_0.model.XtrResponse;
import ee.ria.xtr_2_0.service.XtrServiceWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(XtrController.URL)
@RequiredArgsConstructor
@Slf4j
public class XtrController {

    static final String URL = "/api/v2/xtee";

    private Gson gson = new Gson();

    private final XtrServiceWrapper serviceWrapper;

    /**
     * @param req contains information necessary to perform an XTR request against X-tee data exchange layer
     * @return response depends on the request made
     */
    @PostMapping(value = "get", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<XtrResponse> execute(@RequestBody @Valid XtrRequest req) {
        log.info("Received request: {}", gson.toJson(req));
        return ResponseEntity.ok(serviceWrapper.execute(req));
    }

}
