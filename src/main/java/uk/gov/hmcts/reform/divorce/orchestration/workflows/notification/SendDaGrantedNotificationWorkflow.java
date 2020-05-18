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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendDaGrantedNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.AddDaGrantedCertificateToDocumentsToPrintTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DaGrantedLetterGenerationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DA_GRANTED_OFFLINE_PACK_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.BULK_PRINT_LETTER_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.DOCUMENT_TYPES_TO_PRINT;

@Component
@RequiredArgsConstructor
public class SendDaGrantedNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendDaGrantedNotificationEmailTask sendDaGrantedNotificationEmailTask;
    private final DaGrantedLetterGenerationTask daGrantedLetterGenerationTask;
    private final AddDaGrantedCertificateToDocumentsToPrintTask addDaGrantedCertificateToDocumentsToPrintTask;
    private final BulkPrinterTask bulkPrinterTask;

    private final FeatureToggleService featureToggleService;

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        Map<String, Object> caseData = caseDetails.getCaseData();

        return this.execute(
            getTasks(caseData),
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId()),
            ImmutablePair.of(BULK_PRINT_LETTER_TYPE, DA_GRANTED_OFFLINE_PACK_RESPONDENT),
            ImmutablePair.of(DOCUMENT_TYPES_TO_PRINT, getDocumentTypesToPrint())
        );
    }

    private List<String> getDocumentTypesToPrint() {
        return asList(
            DaGrantedLetterGenerationTask.FileMetadata.DOCUMENT_TYPE,
            AddDaGrantedCertificateToDocumentsToPrintTask.FileMetadata.DOCUMENT_TYPE
        );
    }

    private Task<Map<String, Object>>[] getTasks(Map<String, Object> caseData) {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();
        if (isRespondentUsingDigitalContact(caseData)) {
            tasks.add(sendDaGrantedNotificationEmailTask);
        } else {
            if (featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)) {
                tasks.add(daGrantedLetterGenerationTask);
                tasks.add(addDaGrantedCertificateToDocumentsToPrintTask);
                tasks.add(bulkPrinterTask);
            }
        }

        Task<Map<String, Object>>[] arr = new Task[tasks.size()];

        return tasks.toArray(arr);
    }

    private boolean isRespondentUsingDigitalContact(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty((String) caseData.get(RESP_IS_USING_DIGITAL_CHANNEL)));
    }
}
