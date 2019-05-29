package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_HEARING_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TIME_OF_HEARING_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class SetCourtHearingDetailsFromBulkCaseTest {

    private static String courtName = "Placeholder Court";
    private static String courtHearingDateTime = "2000-01-01T10:20:55.000";
    private static String courtHearingDate = "2000-01-01";
    private static String courtHearingTime = "10:20";

    @InjectMocks
    private SetCourtHearingDetailsFromBulkCase classUnderTest;

    @Test
    public void givenCourtHearingDetailsFromBulkCase_whenSetCourtHearingDetailsOnCcdCase_thenReturnFormattedData() throws TaskException {
        Map<String, Object> caseData = ImmutableMap.of(
            COURT_NAME, courtName,
            COURT_HEARING_DATE, courtHearingDateTime
        );

        CollectionMember<Map<String, Object>> expectedDateTimeCollection = new CollectionMember<>();
        expectedDateTimeCollection.setValue(ImmutableMap.of(
            DATE_OF_HEARING_CCD_FIELD, courtHearingDate,
            TIME_OF_HEARING_CCD_FIELD, courtHearingTime
        ));

        Map<String, Object> expectedResult = ImmutableMap.of(
            COURT_NAME, courtName,
            DATETIME_OF_HEARING_CCD_FIELD, Collections.singletonList(expectedDateTimeCollection)
        );

        TaskContext context = new DefaultTaskContext();

        assertEquals(expectedResult, classUnderTest.execute(context, caseData));
    }
}
