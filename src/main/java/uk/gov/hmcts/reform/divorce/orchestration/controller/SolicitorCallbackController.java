package uk.gov.hmcts.reform.divorce.orchestration.controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.service.SolicitorService;

import java.util.Map;
import javax.ws.rs.core.MediaType;

import static java.util.Collections.singletonList;

@Slf4j
@RequiredArgsConstructor
@RestController
public class SolicitorCallbackController {

    private final SolicitorService solicitorService;

    @PostMapping(path = "/personal-service-pack",
            consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Generates the petitioner's solicitor personal service pack to be served to the respondent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Document generated and emailed to the petitioner's solicitor",
                    response = CaseResponse.class),
            @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<CcdCallbackResponse> issuePersonalServicePack(
            @RequestHeader(value = "Authorization") String authorizationToken,
            @RequestBody @ApiParam("CaseData") CcdCallbackRequest ccdCallbackRequest) {

        Map<String, Object> response;

        try {
            response = solicitorService.issuePersonalServicePack(ccdCallbackRequest, authorizationToken);
        } catch (Exception e) {
            return ResponseEntity.ok(
                    CcdCallbackResponse.builder()
                            .data(ImmutableMap.of())
                            .warnings(ImmutableList.of())
                            .errors(singletonList("Failed to issue solicitor personal service - " + e.getMessage()))
                            .build());
        }
        return ResponseEntity.ok(
                CcdCallbackResponse.builder()
                        .data(response)
                        .build());
    }
}
