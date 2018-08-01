package uk.gov.hmcts.reform.divorce.orchestration.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(value = "Divorce orchestration Service", tags = {"Divorce orchestration Service"})
@Slf4j
public class OrchestrationServiceController {

    @PostMapping("/version/1/orchestrate")
    public String call() {
        return "Orchestration";
    }
}