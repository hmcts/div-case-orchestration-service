package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
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

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.ACCESS_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.INVITATION_FILE_NAME_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_INVITATION_TEMPLATE_NAME;

@Component
public class RespondentLetterGenerator extends ThreadSafeStatefulTask<Map<String, Object>,
    RespondentLetterGenerator.AosInvitationRequest> {

    @Autowired
    private DocumentGeneratorClient documentGeneratorClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {
        GeneratedDocumentInfo aosInvitation =
                documentGeneratorClient.generatePDF(
                        GenerateDocumentRequest.builder()
                                .template(RESPONDENT_INVITATION_TEMPLATE_NAME)
                                .values(ImmutableMap.of(CASE_DETAILS_JSON_KEY,
                                    getState().caseDetails, ACCESS_CODE, caseData.get(PIN)))
                                .build(),
                        getState().authToken
                );

        aosInvitation.setDocumentType(DOCUMENT_TYPE_INVITATION);
        aosInvitation.setFileName(String.format(INVITATION_FILE_NAME_FORMAT, getState().caseDetails.getCaseId()));

        caseData.put(RESPONDENT_INVITATION_TEMPLATE_NAME,aosInvitation);

        return caseData;
    }

    @Data
    @Builder
    public class AosInvitationRequest {
        private CaseDetails caseDetails;
        private String authToken;
    }
}
