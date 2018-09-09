package uk.gov.hmcts.reform.divorce.callback;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.ccd.CcdClientSupport;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJsonToObject;

public abstract class CcdSubmissionSupport extends IntegrationTest {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/issue-petition/";

    @Autowired
    private CcdClientSupport ccdClientSupport;

    protected CaseDetails submitCase(String fileName, UserDetails userDetails) {
        return ccdClientSupport.submitCase(
            loadJsonToObject(PAYLOAD_CONTEXT_PATH + fileName, Map.class),
            userDetails);
    }

    CaseDetails submitCase(String fileName) {
        return submitCase(fileName, createCaseWorkerUser());
    }

    CaseDetails updateCase(String caseId, String fileName, String eventId) {
        return ccdClientSupport.update(caseId,
            fileName == null ? null : loadJsonToObject(PAYLOAD_CONTEXT_PATH + fileName, Map.class),
            eventId, createCaseWorkerUser());
    }

    protected CaseDetails updateCaseForCitizen(String caseId, String fileName, String eventId,
                                               UserDetails userDetails) {
        return ccdClientSupport.updateForCitizen(caseId,
            fileName == null ? null : loadJsonToObject(PAYLOAD_CONTEXT_PATH + fileName, Map.class),
            eventId, userDetails);
    }
}
