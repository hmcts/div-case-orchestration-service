package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.ThreadSafeStatefulTask;

import java.util.Collections;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_FILE_NAME_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_TEMPLATE_NAME;

@Component
public class PetitionGenerator
    extends ThreadSafeStatefulTask<Map<String, Object>, PetitionGenerator.PetitionGenerateRequest> {

    @Autowired
    private DocumentGeneratorClient documentGeneratorClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        GeneratedDocumentInfo miniPetition =
                documentGeneratorClient.generatePDF(
                        GenerateDocumentRequest.builder()
                                .template(MINI_PETITION_TEMPLATE_NAME)
                                .values(Collections.singletonMap(CASE_DETAILS_JSON_KEY,
                                        getState().caseDetails))
                                .build(),
                        getState().authToken
                );


        miniPetition.setDocumentType(DOCUMENT_TYPE_PETITION);
        miniPetition.setFileName(String.format(MINI_PETITION_FILE_NAME_FORMAT, getState().caseDetails.getCaseId()));

        caseData.put(MINI_PETITION_TEMPLATE_NAME,miniPetition);

        return caseData;
    }

    @Data
    @Builder
    public class PetitionGenerateRequest {
        private CaseDetails caseDetails;
        private String authToken;
    }
}
