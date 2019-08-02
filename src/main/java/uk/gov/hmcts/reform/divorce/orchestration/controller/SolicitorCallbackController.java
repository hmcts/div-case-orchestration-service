package uk.gov.hmcts.reform.divorce.orchestration.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.SolicitorService;

import java.util.Map;
import javax.ws.rs.core.MediaType;

@Slf4j
@AllArgsConstructor
@RestController
public class SolicitorCallbackController {

    @Autowired
    private final SolicitorService solicitorService;

    @PostMapping(path = "/personal-service-pack",
            consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Generates the petitioner's solicitor personal service pack to be served to the respondent")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Document generated and emailed to the petitioner's solicitor",
                    response = CaseResponse.class),
            @ApiResponse(code = 400, message = "Bad Request")})
    public ResponseEntity<Map<String, Object>> issuePersonalServicePack(
            @RequestHeader(value = "Authorization") String authorizationToken,
            @PathVariable String caseId,
            @RequestBody @ApiParam("Case data") Map<String, Object> divorceSession)
            throws WorkflowException {

        return ResponseEntity.ok(solicitorService.issuePersonalServicePack(divorceSession, authorizationToken, caseId));
    }
}
