package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentGenerationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.MultipleDocumentGenerationTask;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_GENERATION_REQUESTS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;

@Component
public class IssueAosPackOfflineWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private static final List<DocumentGenerationRequest> RESPONDENT_DOCUMENT_GENERATION_REQUEST_LIST = asList(
        new DocumentGenerationRequest(RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID,
            RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE, RESPONDENT_AOS_INVITATION_LETTER_FILENAME)
    );
    private static final List<DocumentGenerationRequest> CO_RESPONDENT_DOCUMENT_GENERATION_REQUEST_LIST = asList(
        new DocumentGenerationRequest(CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID,
            CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE, CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME)
    );

    @Autowired
    private MultipleDocumentGenerationTask documentsGenerationTask;

    @Autowired
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    public Map<String, Object> run(String authToken, CaseDetails caseDetails, DivorceParty divorceParty) throws WorkflowException {
        return execute(
            new Task[] {
                documentsGenerationTask,
                caseFormatterAddDocuments
            },
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(DOCUMENT_GENERATION_REQUESTS_KEY, getDocumentGenerationRequestsList(divorceParty))
        );
    }

    private List<DocumentGenerationRequest> getDocumentGenerationRequestsList(DivorceParty divorceParty) {
        List<DocumentGenerationRequest> documentGenerationRequestList;

        if (divorceParty.equals(RESPONDENT)) {
            documentGenerationRequestList = RESPONDENT_DOCUMENT_GENERATION_REQUEST_LIST;
        } else if (divorceParty.equals(CO_RESPONDENT)) {
            documentGenerationRequestList = CO_RESPONDENT_DOCUMENT_GENERATION_REQUEST_LIST;
        } else {
            documentGenerationRequestList = emptyList();
        }

        return documentGenerationRequestList;
    }

}