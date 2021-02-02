package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskContextHelper {

    public static TaskContext context() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);

        return context;
    }

    public static TaskContext contextWithToken() {
        TaskContext context = context();
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        return context;
    }

    public static TaskContext contextWithCaseDetails() {
        TaskContext context = context();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, CaseDetails.builder().build());

        return context;
    }

    public static TaskContext contextWithCommonValues() {
        TaskContext context = contextWithToken();
        context.setTransientObject(CASE_DETAILS_JSON_KEY, CaseDetails.builder().caseId(TEST_CASE_ID).build());

        return context;
    }
}
