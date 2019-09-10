package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.GeneratePinRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.idam.client.models.GeneratePinResponse;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_LETTER_HOLDER_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;

@Component
public class CoRespondentPinGenerator implements Task<Map<String, Object>> {

    private final IdamClient idamClient;

    private final AuthUtil authUtil;

    @Autowired
    public CoRespondentPinGenerator(IdamClient idamClient, AuthUtil authUtil) {
        this.idamClient = idamClient;
        this.authUtil = authUtil;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        final GeneratePinResponse coRespondentPin = generateCoRespondentPin(caseData);
        context.setTransientObject(CO_RESPONDENT_PIN, coRespondentPin.getPin());
        caseData.put(CO_RESPONDENT_LETTER_HOLDER_ID, coRespondentPin.getUserId());

        return caseData;
    }

    private GeneratePinResponse generateCoRespondentPin(final Map<String, Object> caseData) {
        GeneratePinRequest pinRequest = new GeneratePinRequest(
            String.valueOf(caseData.getOrDefault(D_8_PETITIONER_FIRST_NAME, "")),
            String.valueOf(caseData.getOrDefault(D_8_PETITIONER_LAST_NAME, "")),
            null
        );

        return idamClient.generatePin(pinRequest, authUtil.getCitizenToken());
    }
}
