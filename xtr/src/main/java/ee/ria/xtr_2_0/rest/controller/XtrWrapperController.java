package ee.ria.xtr_2_0.rest.controller;

import com.google.gson.Gson;
import ee.ria.xtr_2_0.model.XtrRequest;
import ee.ria.xtr_2_0.model.XtrResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class XtrWrapperController {

    final XtrController mainController;
    private final Gson gson = new Gson();

    @PostMapping(value = "/{registry}/{service}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<XtrResponse> execute(@PathVariable String registry,
                                               @PathVariable String service,
                                               @RequestBody @Valid Map<String, Object> req) {
        log.info("Received request: {}", gson.toJson(req));
        XtrRequest request = new XtrRequest(registry, service,
                (String[]) req.getOrDefault("stripCountryPrefix", null),
                (Map<String, Object>) req.getOrDefault("parameters", null),
                (String) req.getOrDefault("multipleInputsFrom", null),
                (String) req.getOrDefault("groupResponseByField", null)
        );
        log.info("Executing request: {}", gson.toJson(request));
        return mainController.execute(request);
    }
}
