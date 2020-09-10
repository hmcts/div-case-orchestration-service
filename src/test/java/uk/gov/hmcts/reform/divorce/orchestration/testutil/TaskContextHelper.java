package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateTestConstants.TEST_CASE_ID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskContextHelper {

    public static TaskContext contextWithToken() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, TEST_CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        return context;
    }
}
