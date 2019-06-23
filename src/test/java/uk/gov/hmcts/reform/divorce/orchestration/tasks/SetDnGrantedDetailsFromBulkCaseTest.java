package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_DETAILS_CONTEXT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_ELIGIBLE_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class SetDnGrantedDetailsFromBulkCaseTest {

    private static final String DN_GRANTED_DATE = "2020-01-01";
    private static final String DA_ELIGIBLE_FROM_DATE = "2021-01-01";
    private static final String PRONOUNCEMENT_JUDGE = "District Judge";

    @InjectMocks
    private SetDnGrantedDetailsFromBulkCase classUnderTest;

    private TaskContext context;

    @Before
    public void setup() {
        context = new DefaultTaskContext();
    }

    @Test
    public void givenDecreeNisiGrantedDateFromBulkCase_whenSetDnGrantedDateOnCcdCase_thenReturnFormattedData() throws TaskException {
        Map<String, Object> bulkCaseDetails = ImmutableMap.of(
                ID, TEST_CASE_ID,
                CCD_CASE_DATA_FIELD, ImmutableMap.of(
                        DECREE_NISI_GRANTED_DATE_CCD_FIELD, DN_GRANTED_DATE,
                        DECREE_ABSOLUTE_ELIGIBLE_DATE_CCD_FIELD, DA_ELIGIBLE_FROM_DATE,
                        PRONOUNCEMENT_JUDGE_CCD_FIELD, PRONOUNCEMENT_JUDGE,
                        "extraField", "extraValueThatShouldBeDropped"
                ));

        CaseDetails caseDetails = CaseDetails.builder()
                .caseData(Collections.emptyMap())
                .build();

        Map<String, Object> expectedResult = ImmutableMap.of(
            DECREE_NISI_GRANTED_DATE_CCD_FIELD, DN_GRANTED_DATE,
            DECREE_ABSOLUTE_ELIGIBLE_DATE_CCD_FIELD, DA_ELIGIBLE_FROM_DATE,
            PRONOUNCEMENT_JUDGE_CCD_FIELD, PRONOUNCEMENT_JUDGE
        );

        context.setTransientObject(CASE_DETAILS_JSON_KEY, caseDetails);
        context.setTransientObject(BULK_CASE_DETAILS_CONTEXT_KEY, bulkCaseDetails);

        assertEquals(expectedResult, classUnderTest.execute(context, new HashMap<>()));
    }
}
