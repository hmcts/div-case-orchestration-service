package uk.gov.hmcts.reform.divorce.support;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;

import java.util.Arrays;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.loadJsonToObject;

public abstract class CcdSubmissionSupport extends IntegrationTest {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/issue-petition/";

    @Autowired
    private CcdClientSupport ccdClientSupport;

    @SuppressWarnings("unchecked")
    @SafeVarargs
    protected final CaseDetails submitCase(String fileName, UserDetails userDetails,
                                     Pair<String, String>... additionalCaseData) {

        final Map caseData = loadJsonToObject(PAYLOAD_CONTEXT_PATH + fileName, Map.class);

        Arrays.stream(additionalCaseData).forEach(
            caseField -> caseData.put(caseField.getKey(), caseField.getValue())
        );

        return ccdClientSupport.submitCase(caseData, userDetails);
    }

    protected CaseDetails submitCase(String fileName) {
        return submitCase(fileName, createCaseWorkerUser());
    }

    protected CaseDetails updateCase(String caseId, String fileName, String eventId) {
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
