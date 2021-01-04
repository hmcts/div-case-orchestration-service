package uk.gov.hmcts.reform.divorce.orchestration.workflows.notification;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStoreTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaGrantedNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DaGrantedCitizenLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DaGrantedSolicitorLetterGenerationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DA_GRANTED_OFFLINE_PACK_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.BULK_PRINT_LETTER_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.DOCUMENT_TYPES_TO_PRINT;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.removeDocumentsByDocumentType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentDigital;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;

@Component
@RequiredArgsConstructor
public class SendDaGrantedNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendDaGrantedNotificationEmailTask sendDaGrantedNotificationEmailTask;

    private final DaGrantedCitizenLetterGenerationTask daGrantedCitizenLetterGenerationTask;
    private final DaGrantedSolicitorLetterGenerationTask daGrantedSolicitorLetterGenerationTask;
    private final FetchPrintDocsFromDmStoreTask fetchPrintDocsFromDmStoreTask;
    private final BulkPrinterTask bulkPrinterTask;

    private final FeatureToggleService featureToggleService;

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        Map<String, Object> incomingCaseData = caseDetails.getCaseData();

        Map<String, Object> caseDataToReturn = this.execute(
            getTasks(incomingCaseData),
            incomingCaseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId()),
            ImmutablePair.of(BULK_PRINT_LETTER_TYPE, DA_GRANTED_OFFLINE_PACK_RESPONDENT),
            ImmutablePair.of(DOCUMENT_TYPES_TO_PRINT, getDocumentTypesToPrint(incomingCaseData))
        );

        return removeDocumentsByDocumentType(caseDataToReturn,
            daGrantedCitizenLetterGenerationTask.getDocumentType(), daGrantedSolicitorLetterGenerationTask.getDocumentType());
    }

    private List<String> getDocumentTypesToPrint(Map<String, Object> caseData) {
        List<String> documentsToPrint = new ArrayList<>();

        if (isRespondentRepresented(caseData)) {
            documentsToPrint.add(daGrantedSolicitorLetterGenerationTask.getDocumentType());
        } else {
            documentsToPrint.add(daGrantedCitizenLetterGenerationTask.getDocumentType());
        }
        documentsToPrint.add(DECREE_ABSOLUTE_DOCUMENT_TYPE);

        return documentsToPrint;
    }

    private Task<Map<String, Object>>[] getTasks(Map<String, Object> caseData) {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();
        if (isRespondentDigital(caseData)) {
            tasks.add(sendDaGrantedNotificationEmailTask);
        } else {
            if (featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)) {
                if (isRespondentRepresented(caseData)) {
                    tasks.add(daGrantedSolicitorLetterGenerationTask);
                } else {
                    tasks.add(daGrantedCitizenLetterGenerationTask);
                }
                tasks.add(fetchPrintDocsFromDmStoreTask);
                tasks.add(bulkPrinterTask);
            }
        }

        Task<Map<String, Object>>[] arr = new Task[tasks.size()];

        return tasks.toArray(arr);
    }

}
