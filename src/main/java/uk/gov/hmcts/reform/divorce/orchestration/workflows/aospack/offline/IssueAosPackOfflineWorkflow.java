package uk.gov.hmcts.reform.divorce.orchestration.workflows.aospack.offline;

import lombok.extern.slf4j.Slf4j;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_GENERATION_REQUESTS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_AOS_INVITATION_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TWO_YEAR_SEPARATION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TWO_YEAR_SEPARATION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.TWO_YEAR_SEPARATION_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;

@Component
@Slf4j
public class IssueAosPackOfflineWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private static final DocumentGenerationRequest RESPONDENT_AOS_INVITATION_LETTER = new DocumentGenerationRequest(
        RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID, RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE, RESPONDENT_AOS_INVITATION_LETTER_FILENAME);

    private static final DocumentGenerationRequest CO_RESPONDENT_AOS_INVITATION_LETTER = new DocumentGenerationRequest(
        CO_RESPONDENT_AOS_INVITATION_LETTER_TEMPLATE_ID, CO_RESPONDENT_AOS_INVITATION_LETTER_DOCUMENT_TYPE,
        CO_RESPONDENT_AOS_INVITATION_LETTER_FILENAME);

    @Autowired
    private MultipleDocumentGenerationTask documentsGenerationTask;

    @Autowired
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    public Map<String, Object> run(String authToken, CaseDetails caseDetails, DivorceParty divorceParty) throws WorkflowException {
        Map<String, Object> caseData = caseDetails.getCaseData();
        String reasonForDivorce = (String) caseData.get(D_8_REASON_FOR_DIVORCE);

        return execute(
            new Task[] {
                documentsGenerationTask,
                caseFormatterAddDocuments
            },
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(DOCUMENT_GENERATION_REQUESTS_KEY, getDocumentGenerationRequestsList(divorceParty, reasonForDivorce))
        );
    }

    private List<DocumentGenerationRequest> getDocumentGenerationRequestsList(DivorceParty divorceParty, String reasonForDivorce) {
        List<DocumentGenerationRequest> documentGenerationRequestList = new ArrayList<>();

        if (divorceParty.equals(RESPONDENT)) {
            documentGenerationRequestList.add(RESPONDENT_AOS_INVITATION_LETTER);
            log.debug("reasonForDivorce is {}", reasonForDivorce);
            if (SEPARATION_TWO_YEARS.equals(reasonForDivorce)) {
                documentGenerationRequestList.add(new DocumentGenerationRequest(TWO_YEAR_SEPARATION_TEMPLATE_ID,
                    TWO_YEAR_SEPARATION_DOCUMENT_TYPE,
                    TWO_YEAR_SEPARATION_FILENAME));
            }
        } else if (divorceParty.equals(CO_RESPONDENT)) {
            documentGenerationRequestList.add(CO_RESPONDENT_AOS_INVITATION_LETTER);
        } else {
            documentGenerationRequestList = emptyList();
        }

        return documentGenerationRequestList;
    }

}