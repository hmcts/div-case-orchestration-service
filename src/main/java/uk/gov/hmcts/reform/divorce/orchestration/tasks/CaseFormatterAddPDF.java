package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_INVITATION_TEMPLATE_NAME;

@Component
public class CaseFormatterAddPDF implements Task<Map<String, Object>> {
    private final CaseFormatterClient caseFormatterClient;

    @Autowired
    public CaseFormatterAddPDF(CaseFormatterClient caseFormatterClient) {
        this.caseFormatterClient = caseFormatterClient;
    }

    @Override
    public Map<String, Object> execute(TaskContext context,
                                       Map<String, Object> caseData) {
        GeneratedDocumentInfo miniPetition
                = (GeneratedDocumentInfo) context.getTransientObject(MINI_PETITION_TEMPLATE_NAME);
        GeneratedDocumentInfo respondentInvitation
                = (GeneratedDocumentInfo) context.getTransientObject(RESPONDENT_INVITATION_TEMPLATE_NAME);

        return caseFormatterClient.addDocuments(
                DocumentUpdateRequest.builder()
                        .caseData(caseData)
                        .documents(ImmutableList.of(miniPetition,
                                respondentInvitation))
                        .build());
    }
}
