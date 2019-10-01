package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.formatter.service.CaseFormatterService;
import uk.gov.hmcts.reform.divorce.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CaseDataResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA;

@Component
public class CaseDataToDivorceFormatterTask implements Task<CaseDataResponse> {
    private final CaseFormatterService caseFormatterService;
    private final ObjectMapper objectMapper;

    @Autowired
    public CaseDataToDivorceFormatterTask(CaseFormatterService caseFormatterService, ObjectMapper objectMapper) {
        this.caseFormatterService = caseFormatterService;
        this.objectMapper = objectMapper;
    }

    @Override
    public CaseDataResponse execute(TaskContext context, CaseDataResponse caseDataResponse) {
        Map<String, Object> data = context.getTransientObject(CCD_CASE_DATA);
        DivorceSession divorceData = caseFormatterService.transformToDivorceSession(
            objectMapper.convertValue(data, CoreCaseData.class)
        );

        caseDataResponse.setData(objectMapper.convertValue(divorceData, Map.class));

        return caseDataResponse;
    }
}