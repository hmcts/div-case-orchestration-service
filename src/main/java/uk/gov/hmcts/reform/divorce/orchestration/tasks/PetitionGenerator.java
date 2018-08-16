package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.*;

@Component
public class PetitionGenerator implements Task<Map<String, Object>> {
    @Autowired
    private DocumentGeneratorClient documentGeneratorClient;

    private String authToken;

    private CaseDetails caseDetails;

    public void setup(String authToken, CaseDetails caseDetails) {
        this.authToken = authToken;
        this.caseDetails = caseDetails;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        GeneratedDocumentInfo miniPetition =
                documentGeneratorClient.generatePDF(
                        GenerateDocumentRequest.builder()
                                .template(MINI_PETITION_TEMPLATE_NAME)
                                .values(Collections.singletonMap(CASE_DETAILS_JSON_KEY,
                                        caseDetails))
                                .build(),
                        authToken
                );


        miniPetition.setDocumentType(DOCUMENT_TYPE_PETITION);
        miniPetition.setFileName(String.format(MINI_PETITION_FILE_NAME_FORMAT, caseDetails.getCaseId()));

        caseData.put(MINI_PETITION_TEMPLATE_NAME,miniPetition);

        return caseData;
    }
}
