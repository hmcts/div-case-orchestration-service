package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA;

@Component
public class CaseDataToDivorceFormatter implements Task<CaseDataResponse> {
    private final CaseFormatterClient caseFormatterClient;

    @Autowired
    public CaseDataToDivorceFormatter(CaseFormatterClient caseFormatterClient) {
        this.caseFormatterClient = caseFormatterClient;
    }

    @Override
    public CaseDataResponse execute(TaskContext context, CaseDataResponse caseDataResponse) {

        caseDataResponse.setData(
            caseFormatterClient.transformToDivorceFormat(
                context.getTransientObject(AUTH_TOKEN_JSON_KEY),
                context.getTransientObject(CCD_CASE_DATA)
            ));

        return caseDataResponse;
    }
}