package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.client.IdamClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.PinRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.Map;

@Component
public class IdamPinGenerator implements Task<Map<String, Object>> {
    private static final String D_8_PETITIONER_FIRST_NAME = "D8PetitionerFirstName";
    private static final String D_8_PETITIONER_LAST_NAME = "D8PetitionerLastName";
    private static final String RESPONDENT_LETTER_HOLDER_ID = "respondentLetterHolderId";
    public static final String PIN = "pin";

    @Autowired
    private IdamClient idamClient;

    private String authToken;

    public void setup(String authToken) {
        this.authToken = authToken;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        Pin pin = idamClient.createPin(PinRequest.builder()
                        .firstName(String.valueOf(caseData.getOrDefault(D_8_PETITIONER_FIRST_NAME, "")))
                        .lastName(String.valueOf(caseData.getOrDefault(D_8_PETITIONER_LAST_NAME, "")))
                        .build(),
                authToken);

        caseData.put(PIN, pin.getPin());
        caseData.put(RESPONDENT_LETTER_HOLDER_ID, pin.getUserId());

        return caseData;
    }
}
