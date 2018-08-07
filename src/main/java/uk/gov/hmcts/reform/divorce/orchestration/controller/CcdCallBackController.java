package uk.gov.hmcts.reform.divorce.orchestration.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.domian.model.ccd.CCDCallbackResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domian.model.ccd.CreateEvent;
import uk.gov.hmcts.reform.divorce.orchestration.service.PetitionIssuedCallBackService;

import javax.ws.rs.core.MediaType;

@RestController
@RequestMapping(consumes = MediaType.APPLICATION_JSON, produces = MediaType.APPLICATION_JSON)
public class CcdCallBackController {
    @Autowired
    private PetitionIssuedCallBackService petitionIssuedCallBackService;

    @PostMapping(path = "/petition-issued")
    @ApiOperation(value = "Handles Issue callback from CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Callback was processed successFully or in case of an error message is "
            + "attached to the case",
            response = CCDCallbackResponse.class),
        @ApiResponse(code = 400, message = "Bad Request")
    })
    public ResponseEntity<CCDCallbackResponse> petitionIssued(
        @RequestHeader(value = "Authorization") String authorizationToken,
        @RequestBody @ApiParam("CaseData") CreateEvent caseDetailsRequest) {
        return ResponseEntity.ok(
            petitionIssuedCallBackService.issuePetition(caseDetailsRequest.getCaseDetails().getCaseData(),
                authorizationToken));
    }
}
