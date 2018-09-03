package uk.gov.hmcts.reform.divorce.callback;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.support.ccd.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Map;

public abstract class CcdSubmissionSupport extends IntegrationTest {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/issue-petition/";

    @Autowired
    private CcdClientSupport ccdClientSupport;

    CaseDetails submitCase(String fileName) {
        return ccdClientSupport.submitCase(
            ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + fileName, Map.class),
            createCaseWorkerUser());
    }

    CaseDetails updateCase(String caseId, String fileName, String eventId) {
        return ccdClientSupport.update(caseId,
            ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + fileName, Map.class),
            eventId, createCaseWorkerUser());
    }
}
