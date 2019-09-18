package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.formatter.service.CaseFormatterService;
import uk.gov.hmcts.reform.divorce.model.DivorceCaseWrapper;
import uk.gov.hmcts.reform.divorce.model.ccd.CoreCaseData;
import uk.gov.hmcts.reform.divorce.model.usersession.DivorceSession;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_CLARIFICATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;

@RequiredArgsConstructor
@Component
public class FormatDivorceSessionToDnCaseData implements Task<Map<String, Object>> {

    private final CaseFormatterService caseFormatterService;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> sessionData) {
        CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

        CoreCaseData coreCaseData = objectMapper.convertValue(caseDetails.getCaseData(), CoreCaseData.class);
        DivorceSession divorceSession = objectMapper.convertValue(sessionData, DivorceSession.class);

        if (AWAITING_CLARIFICATION.equalsIgnoreCase(caseDetails.getState())) {
            DivorceCaseWrapper divorceCaseWrapper = new DivorceCaseWrapper(coreCaseData, divorceSession);
            return objectMapper.convertValue(caseFormatterService.getDnClarificationCaseData(divorceCaseWrapper), Map.class);
        } else {
            return objectMapper.convertValue(caseFormatterService.getDnCaseData(divorceSession), Map.class);
        }
    }
}
