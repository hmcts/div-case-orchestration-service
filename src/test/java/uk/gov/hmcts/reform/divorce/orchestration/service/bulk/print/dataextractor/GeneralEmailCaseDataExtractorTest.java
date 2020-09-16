package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_DETAILS;

public class GeneralEmailCaseDataExtractorTest {

    @Test
    public void getSolicitorReferenceReturnsValidValueWhenItExists() {
        Map<String, Object> caseData = buildCaseDataWithGeneralDetails(TEST_GENERAL_EMAIL_DETAILS);
        assertThat(GeneralEmailCaseDataExtractor.getGeneralEmailDetails(caseData), is(TEST_GENERAL_EMAIL_DETAILS));
    }

    @Test
    public void getSolicitorReferenceThrowsExceptionsWhenItIsNull() throws TaskException {
        Map<String, Object> caseData = buildCaseDataWithGeneralDetails(null);

        TaskException exception = assertThrows(TaskException.class,
            () -> GeneralEmailCaseDataExtractor.getGeneralEmailDetails(caseData)
        );
        assertThat(
            exception.getMessage(),
            containsString(format("Could not evaluate value of mandatory property \"%s\"", GENERAL_EMAIL_DETAILS))
        );
    }

    private static Map<String, Object> buildCaseDataWithGeneralDetails(String generalDetails) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(GENERAL_EMAIL_DETAILS, generalDetails);

        return caseData;
    }
}