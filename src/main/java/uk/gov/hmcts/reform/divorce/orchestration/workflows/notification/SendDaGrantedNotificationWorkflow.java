package uk.gov.hmcts.reform.divorce.orchestration.workflows.notification;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentGenerationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationForPreparedDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.PrepareDataForDaGrantedLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaGrantedNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DA_GRANTED_OFFLINE_PACK_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_LETTER_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_GENERATION_REQUESTS_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinter.BULK_PRINT_LETTER_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinter.DOCUMENT_TYPES_TO_PRINT;

@Component
@RequiredArgsConstructor
public class SendDaGrantedNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendDaGrantedNotificationEmailTask sendDaGrantedNotificationEmailTask;
    private final PrepareDataForDaGrantedLetterGenerationTask prepareDataForDaGrantedLetterTask;
    private final DocumentGenerationForPreparedDataTask documentGenerationForPreparedDataTask;
    private final BulkPrinter bulkPrinter;

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        Map<String, Object> caseData = caseDetails.getCaseData();
        List<DocumentGenerationRequest> documentGenerationRequestsList = getDocumentGenerationRequestsList();
        final List<String> documentTypesToPrint = documentGenerationRequestsList.stream()
            .map(DocumentGenerationRequest::getDocumentType)
            .collect(Collectors.toList());

        List<Task> tasks = new ArrayList<>();
        if (isDigitalProcess(caseData)) {
            tasks.add(sendDaGrantedNotificationEmailTask);
        } else {
            tasks.add(prepareDataForDaGrantedLetterTask);
            tasks.add(documentGenerationForPreparedDataTask);
            tasks.add(bulkPrinter);
        }

        Task[] taskArr = new Task[tasks.size()];
        return this.execute(
            tasks.toArray(taskArr),
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(DOCUMENT_GENERATION_REQUESTS_KEY, documentGenerationRequestsList),
            ImmutablePair.of(BULK_PRINT_LETTER_TYPE, DA_GRANTED_OFFLINE_PACK_RESPONDENT),
            ImmutablePair.of(DOCUMENT_TYPES_TO_PRINT, documentTypesToPrint)
        );
    }

    private List<DocumentGenerationRequest> getDocumentGenerationRequestsList() {
        List<DocumentGenerationRequest> documentGenerationRequestList = new ArrayList<>();

        DocumentGenerationRequest daGrantedCoverLetter = new DocumentGenerationRequest(
            DECREE_ABSOLUTE_LETTER_TEMPLATE_ID,
            DECREE_ABSOLUTE_LETTER_DOCUMENT_TYPE,
            DECREE_ABSOLUTE_LETTER_FILENAME);

        DocumentGenerationRequest daGrantedDocument = new DocumentGenerationRequest(
            DECREE_ABSOLUTE_TEMPLATE_ID,
            DECREE_ABSOLUTE_DOCUMENT_TYPE,
            DECREE_ABSOLUTE_FILENAME);

        documentGenerationRequestList.add(daGrantedCoverLetter);
        documentGenerationRequestList.add(daGrantedDocument);

        return documentGenerationRequestList;
    }

    private boolean isDigitalProcess(Map<String, Object> caseData) {
        String respContactMethodIsDigital = (String) caseData.get(RESP_IS_USING_DIGITAL_CHANNEL);
        return YES_VALUE.equalsIgnoreCase(respContactMethodIsDigital);
    }
}
