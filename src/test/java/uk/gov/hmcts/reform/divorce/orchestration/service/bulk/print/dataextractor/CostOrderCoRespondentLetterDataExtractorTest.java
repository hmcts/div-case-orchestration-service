package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;

public class CostOrderCoRespondentLetterDataExtractorTest {

    public static final String SOLICITOR_REFERENCE = "SolRef123";

    @Test
    public void getSolicitorReferenceWith_ValidFormat() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, SOLICITOR_REFERENCE);

        assertThat(CostOrderCoRespondentLetterDataExtractor.getSolicitorReference(caseData), is(SOLICITOR_REFERENCE));
    }
}
