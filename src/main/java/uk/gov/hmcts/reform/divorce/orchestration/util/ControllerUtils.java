package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ControllerUtils {

    public static ResponseEntity<CcdCallbackResponse> responseWithData(Map<String, Object> data) {
        return ResponseEntity.ok(ccdResponseWithData(data));
    }

    public static ResponseEntity<CcdCallbackResponse> responseWithErrors(List<String> errors) {
        return ResponseEntity.ok(CcdCallbackResponse.builder()
            .errors(errors)
            .build());
    }

    public static CcdCallbackRequest ccdRequestWithData(Map<String, Object> caseData) {
        return CcdCallbackRequest.builder()
            .caseDetails(caseDetailsWithData(caseData))
            .build();
    }

    public static CcdCallbackResponse ccdResponseWithData(Map<String, Object> caseData) {
        return CcdCallbackResponse.builder()
            .data(caseData)
            .build();
    }

    public static CaseDetails caseDetailsWithData(Map<String, Object> caseData) {
        return CaseDetails.builder()
            .caseData(caseData)
            .build();
    }

}