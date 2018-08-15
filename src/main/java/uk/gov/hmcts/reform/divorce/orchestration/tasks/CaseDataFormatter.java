package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

@Component
public class CaseDataFormatter implements Task<Map<String, Object>> {
    private static final String MINI_PETITION_TEMPLATE_NAME = "divorceminipetition";
    private static final String RESPONDENT_INVITATION_TEMPLATE_NAME = "aosinvitation";

    @Autowired
    private CaseFormatterClient caseFormatterClient;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        return caseFormatterClient.addDocuments(
                DocumentUpdateRequest.builder()
                        .caseData(caseData)
                        .documents(ImmutableList.of((GeneratedDocumentInfo) caseData.get(MINI_PETITION_TEMPLATE_NAME), (GeneratedDocumentInfo) caseData.get(RESPONDENT_INVITATION_TEMPLATE_NAME)))
                        .build());
    }
}
