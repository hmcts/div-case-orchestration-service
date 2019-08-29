package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_ADDITIONAL_INFO_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TYPE_COSTS_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;

public class RemoveLegalAdvisorMakeDecisionFieldsTaskTest {
    
    private RemoveLegalAdvisorMakeDecisionFieldsTask classToTest = new RemoveLegalAdvisorMakeDecisionFieldsTask();

    @Test
    public void testExecuteRemoveLegalAdvisorMakeDecisionFieldsTask() throws TaskException {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put("anyKey", "anyData");
        caseData.putAll(ImmutableMap.of(
            DECREE_NISI_GRANTED_CCD_FIELD, "Yes",
            DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD, "Yes",
            WHO_PAYS_COSTS_CCD_FIELD, "respondent",
            TYPE_COSTS_DECISION_CCD_FIELD, "info",
            COSTS_ORDER_ADDITIONAL_INFO_CCD_FIELD, "some text"
        ));

        Map<String, Object> response = classToTest.execute(null, caseData);

        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("anyKey", "anyData");
        assertThat(response, is(expectedMap));
    }
}
