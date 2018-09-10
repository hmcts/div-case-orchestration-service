package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@Component
@Slf4j
public class CaseDataToDivorceFormatter implements Task<CaseDataResponse> {
    private final CaseFormatterClient caseFormatterClient;

    @Autowired
    public CaseDataToDivorceFormatter(CaseFormatterClient caseFormatterClient) {
        this.caseFormatterClient = caseFormatterClient;
    }

    @Override
    public CaseDataResponse execute(TaskContext context, CaseDataResponse caseDataResponse) {

        log.warn("******this line it hit*******");

        caseDataResponse.setData(
            caseFormatterClient.transformToDivorceFormat(
                String.valueOf(context.getTransientObject(AUTH_TOKEN_JSON_KEY)),
                caseDataResponse.getData()
            ));

        return caseDataResponse;
    }
}