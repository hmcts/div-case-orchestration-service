package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

@Component
public class RespondentLetterGenerator implements Task<Map<String, Object>> {
    private static final String CASE_DETAILS_JSON_KEY = "caseDetails";
    private static final String DOCUMENT_TYPE_INVITATION = "aosinvitation";
    private static final String RESPONDENT_INVITATION_TEMPLATE_NAME = "aosinvitation";
    private static final String INVITATION_FILE_NAME_FORMAT = "aosinvitation%s";
    private static final String ACCESS_CODE = "access_code";
    public static final String PIN = "pin";

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
        GeneratedDocumentInfo aosinvitation =
                documentGeneratorClient.generatePDF(
                        GenerateDocumentRequest.builder()
                                .template(RESPONDENT_INVITATION_TEMPLATE_NAME)
                                .values(ImmutableMap.of(CASE_DETAILS_JSON_KEY, caseDetails, ACCESS_CODE, caseData.get(PIN)))
                                .build(),
                        authToken
                );

        aosinvitation.setDocumentType(DOCUMENT_TYPE_INVITATION);
        aosinvitation.setFileName(String.format(INVITATION_FILE_NAME_FORMAT, caseDetails.getCaseId()));

        caseData.put(RESPONDENT_INVITATION_TEMPLATE_NAME,aosinvitation);

        return caseData;
    }
}
