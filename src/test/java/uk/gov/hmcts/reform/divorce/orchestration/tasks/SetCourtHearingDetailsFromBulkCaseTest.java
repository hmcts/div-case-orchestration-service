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
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_HEARING_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TIME_OF_HEARING_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class SetCourtHearingDetailsFromBulkCaseTest {

    private static final String COURT_NAME = "Placeholder Court";
    private static final String COURT_HEARING_DATE_TIME = "2000-01-01T10:20:55.000";
    private static final String COURT_HEARING_DATE = "2000-01-01";
    private static final String COURT_HEARING_TIME = "10:20";

    @InjectMocks
    private SetCourtHearingDetailsFromBulkCase classUnderTest;

    private TaskContext context;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
    }

    @Test
    public void givenCourtHearingDetailsFromBulkCase_whenSetCourtHearingDetailsOnCcdCase_thenReturnFormattedData() throws TaskException {
        Map<String, Object> bulkCaseData = ImmutableMap.of(
            ID, TEST_CASE_ID,
            CCD_CASE_DATA_FIELD, ImmutableMap.of(
                COURT_NAME_CCD_FIELD, COURT_NAME,
                COURT_HEARING_DATE_CCD_FIELD, COURT_HEARING_DATE_TIME
            ));

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(Collections.emptyMap())
                .build();

        CollectionMember<Map<String, Object>> expectedDateTimeCollection = new CollectionMember<>();
        expectedDateTimeCollection.setValue(ImmutableMap.of(
            DATE_OF_HEARING_CCD_FIELD, COURT_HEARING_DATE,
            TIME_OF_HEARING_CCD_FIELD, COURT_HEARING_TIME
        ));

        Map<String, Object> expectedResult = ImmutableMap.of(
            COURT_NAME_CCD_FIELD, COURT_NAME,
            DATETIME_OF_HEARING_CCD_FIELD, Collections.singletonList(expectedDateTimeCollection)
        );

        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        assertEquals(expectedResult, classUnderTest.execute(context, bulkCaseData));
    }

    @Test
    public void givenCourtHearingDetailsFromBulkCase_whenSetCourtHearingDetailsOnCcdCaseWithExistingHearing_thenReturnFormattedData() throws TaskException {
        Map<String, Object> caseData = ImmutableMap.of(
            ID, TEST_CASE_ID,
            CCD_CASE_DATA_FIELD, ImmutableMap.of(
            COURT_NAME_CCD_FIELD, COURT_NAME,
            COURT_HEARING_DATE_CCD_FIELD, COURT_HEARING_DATE_TIME
        ));

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
            DATE_OF_HEARING_CCD_FIELD, COURT_HEARING_DATE,
            TIME_OF_HEARING_CCD_FIELD, COURT_HEARING_TIME
        ));

        courtHearings.add(newDateTimeCollection);

        Map<String, Object> expectedResult = ImmutableMap.of(
            COURT_NAME_CCD_FIELD, COURT_NAME,
            DATETIME_OF_HEARING_CCD_FIELD, courtHearings
        );

        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);

        assertEquals(expectedResult, classUnderTest.execute(context, caseData));
    }
}
