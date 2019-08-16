package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COURT_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_LISTING_CASE_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATETIME_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DATE_OF_HEARING_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PRONOUNCEMENT_JUDGE_CCD_FIELD;

@RunWith(MockitoJUnitRunner.class)
public class RemoveListingDataTaskUTest {

    @Spy
    private ObjectMapper mapper;

    @InjectMocks
    private RemoveListingDataTask classToTest;

    @Test
    public void testExecuteTaskRemoveListingDataIsRemoved() throws TaskException {
        CollectionMember<Map<String, Object>> hearingDate = new CollectionMember<>();
        hearingDate.setValue(Collections.singletonMap(DATE_OF_HEARING_CCD_FIELD, "2020-02-22"));

        List<CollectionMember<Map<String, Object>>> courtHearingDates = new ArrayList<>();
        courtHearingDates.add(hearingDate);
        courtHearingDates.add(hearingDate);

        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("anyKey", "anyData");

        List<CollectionMember<Map<String, Object>>> expectedCourtHearingDates = new ArrayList<>();
        expectedCourtHearingDates.add(hearingDate);
        expectedMap.put(DATETIME_OF_HEARING_CCD_FIELD, expectedCourtHearingDates);

        Map<String, Object> caseData = ImmutableMap.of("anyKey", "anyData",
                COURT_NAME_CCD_FIELD, "Court",
                BULK_LISTING_CASE_ID_FIELD,"caseLink",
                PRONOUNCEMENT_JUDGE_CCD_FIELD, "Judge Name",
                DATETIME_OF_HEARING_CCD_FIELD, courtHearingDates);

        Map<String, Object> response = classToTest.execute(null, caseData);
        assertThat(response, is(expectedMap));
    }
}
