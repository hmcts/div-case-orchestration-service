package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_REFERENCE_JSON_KEY;


public class CostOrderCoRespondentLetterDataExtractorTest {

    private static final String EXPECTED_HEARING_DATE = "20 July 2020";
    private static final String HEARING_DATE_CCD_FORMAT = "2020-07-20";
    public static final String SOLICITOR_REFERENCE = "SolRef123";

    @Test
    public void getHearingDateReturnsReadableFormat() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(DATETIME_OF_HEARING_CCD_FIELD, HEARING_DATE_CCD_FORMAT);

        assertThat(CostOrderCoRespondentLetterDataExtractor.getHearingDate(caseData), is(EXPECTED_HEARING_DATE));
    }

    @Test
    public void getSolicitorReferenceWith_ValidFormat() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_REFERENCE_JSON_KEY, SOLICITOR_REFERENCE);

        assertThat(CostOrderCoRespondentLetterDataExtractor.getSolicitorReference(caseData), is(SOLICITOR_REFERENCE));
    }
}
