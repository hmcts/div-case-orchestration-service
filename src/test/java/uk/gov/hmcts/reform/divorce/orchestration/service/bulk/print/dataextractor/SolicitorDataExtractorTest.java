package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor.CaseDataKeys.SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrintTestData.SOLICITOR_REF;

public class SolicitorDataExtractorTest {

    @Test
    public void getSolicitorReferenceReturnsValidValueWhenItExists() {
        Map<String, Object> caseData = buildCaseDataWithSolicitorReference(SOLICITOR_REF);
        assertThat(SolicitorDataExtractor.getSolicitorReference(caseData), is(SOLICITOR_REF));
    }

    @Test
    public void getSolicitorReferenceReturnsEmptyStringWhenItIsEmpty() {
        Map<String, Object> caseData = buildCaseDataWithSolicitorReference("");
        assertThat(SolicitorDataExtractor.getSolicitorReference(caseData), is(""));
    }

    @Test
    public void getSolicitorReferenceThrowsExceptionsWhenItIsNull() {
        Map<String, Object> caseData = buildCaseDataWithSolicitorReference(null);
        assertThat(SolicitorDataExtractor.getSolicitorReference(caseData), is(""));
    }

    private static Map<String, Object> buildCaseDataWithSolicitorReference(String solicitorReference) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(SOLICITOR_REFERENCE, solicitorReference);

        return caseData;
    }
}
