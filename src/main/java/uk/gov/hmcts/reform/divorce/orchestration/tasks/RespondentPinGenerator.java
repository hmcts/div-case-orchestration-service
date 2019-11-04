package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.GeneratePinRequest;
import uk.gov.hmcts.reform.idam.client.models.GeneratePinResponse;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_PIN;

@Component
public class RespondentPinGenerator implements Task<Map<String, Object>> {

    private final IdamClient idamClient;

    private final AuthUtil authUtil;

    @Autowired
    public RespondentPinGenerator(IdamClient idamClient, AuthUtil authUtil) {
        this.idamClient = idamClient;
        this.authUtil = authUtil;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        final GeneratePinResponse respondentPin = generateRespondentPin(caseData);
        context.setTransientObject(RESPONDENT_PIN, respondentPin.getPin());
        caseData.put(RESPONDENT_LETTER_HOLDER_ID, respondentPin.getUserId());

        return caseData;
    }

    private GeneratePinResponse generateRespondentPin(final Map<String, Object> caseData) {
        GeneratePinRequest pinRequest = new GeneratePinRequest(
            String.valueOf(caseData.getOrDefault(D_8_PETITIONER_FIRST_NAME, "")),
            String.valueOf(caseData.getOrDefault(D_8_PETITIONER_LAST_NAME, "")),
            null
        );
        return idamClient.generatePin(pinRequest, authUtil.getCitizenToken());
    }
}
