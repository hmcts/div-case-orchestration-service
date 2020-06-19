package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoELetterDataExtractor.CaseDataKeys.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.SOLICITOR_REF;

public class SolicitorDataExtractorTest {

    @Test
    public void getSolicitorReferenceReturnsValidValueWhenItExists() {
        Map<String, Object> caseData = buildCaseDataWithSolicitorReference(SOLICITOR_REF);
        assertThat(SolicitorDataExtractor.getSolicitorReference(caseData), is(SOLICITOR_REF));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getSolicitorReferenceThrowsExceptionsWhenItIsEmpty() {
        SolicitorDataExtractor.getSolicitorReference(buildCaseDataWithSolicitorReference(""));
    }

    @Test(expected = InvalidDataForTaskException.class)
    public void getSolicitorReferenceThrowsExceptionsWhenItIsNull() {
        SolicitorDataExtractor.getSolicitorReference(buildCaseDataWithSolicitorReference(null));
    }

    private static Map<String, Object> buildCaseDataWithSolicitorReference(String solicitorReference) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_REFERENCE, solicitorReference);

        return caseData;
    }

}