package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_HEARING_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
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

    private TaskContext context;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
    }

    @Test
    public void givenCourtHearingDetailsFromBulkCase_whenSetCourtHearingDetailsOnCcdCase_thenReturnFormattedData() throws TaskException {
        Map<String, Object> caseData = ImmutableMap.of(
            COURT_NAME, courtName,
            COURT_HEARING_DATE, courtHearingDateTime
        );

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(Collections.emptyMap())
                .build();

        CollectionMember<Map<String, Object>> expectedDateTimeCollection = new CollectionMember<>();
        expectedDateTimeCollection.setValue(ImmutableMap.of(
            DATE_OF_HEARING_CCD_FIELD, courtHearingDate,
            TIME_OF_HEARING_CCD_FIELD, courtHearingTime
        ));

        Map<String, Object> expectedResult = ImmutableMap.of(
            COURT_NAME, courtName,
            DATETIME_OF_HEARING_CCD_FIELD, Collections.singletonList(expectedDateTimeCollection)
        );

        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        assertEquals(expectedResult, classUnderTest.execute(context, caseData));
    }

    @Test
    public void givenCourtHearingDetailsFromBulkCase_whenSetCourtHearingDetailsOnCcdCaseWithExistingHearing_thenReturnFormattedData() throws TaskException {
        Map<String, Object> caseData = ImmutableMap.of(
                COURT_NAME, courtName,
                COURT_HEARING_DATE, courtHearingDateTime
        );

        CollectionMember<Map<String, Object>> existingDateTimeCollection = new CollectionMember<>();
        existingDateTimeCollection.setValue(ImmutableMap.of(
                DATE_OF_HEARING_CCD_FIELD, "1999-10-10",
                TIME_OF_HEARING_CCD_FIELD, "12:55"
        ));

        List<CollectionMember> courtHearings = new ArrayList<>();
        courtHearings.add(existingDateTimeCollection);

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(Collections.singletonMap(DATETIME_OF_HEARING_CCD_FIELD, courtHearings))
                .build();

        CollectionMember<Map<String, Object>> newDateTimeCollection = new CollectionMember<>();
        newDateTimeCollection.setValue(ImmutableMap.of(
                DATE_OF_HEARING_CCD_FIELD, courtHearingDate,
                TIME_OF_HEARING_CCD_FIELD, courtHearingTime
        ));

        courtHearings.add(newDateTimeCollection);

        Map<String, Object> expectedResult = ImmutableMap.of(
                COURT_NAME, courtName,
                DATETIME_OF_HEARING_CCD_FIELD, courtHearings
        );

        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        assertEquals(expectedResult, classUnderTest.execute(context, caseData));
    }
}
