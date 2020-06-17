package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCoRespondentGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentSolicitorCoverLetterGenerationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COST_ORDER_OFFLINE_PACK_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.BULK_PRINT_LETTER_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.DOCUMENT_TYPES_TO_PRINT;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.removeDocumentByDocumentType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentDigital;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentLiableForCosts;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentRepresented;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCostsClaimGranted;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendDnPronouncedNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendPetitionerGenericUpdateNotificationEmailTask sendPetitionerGenericUpdateNotificationEmailTask;
    private final SendRespondentGenericUpdateNotificationEmailTask sendRespondentGenericUpdateNotificationEmailTask;
    private final SendCoRespondentGenericUpdateNotificationEmailTask sendCoRespondentGenericUpdateNotificationEmailTask;

    private final CostOrderCoRespondentCoverLetterGenerationTask costOrderCoRespondentCoverLetterGenerationTask;
    private final CostOrderCoRespondentSolicitorCoverLetterGenerationTask costOrderCoRespondentSolicitorCoverLetterGenerationTask;
    private final FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;
    private final BulkPrinterTask bulkPrinterTask;
    private final FeatureToggleService featureToggleService;

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = caseDetails.getCaseData();

        Map<String, Object> returnCaseData = this.execute(
            getTasks(caseDetails),
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(BULK_PRINT_LETTER_TYPE, COST_ORDER_OFFLINE_PACK_CO_RESPONDENT),
            ImmutablePair.of(DOCUMENT_TYPES_TO_PRINT, getDocumentTypesToPrint(caseData))
        );

        return removeDocumentByDocumentType(returnCaseData, coverLetterDocument(returnCaseData));
    }

    private List<String> getDocumentTypesToPrint(Map<String, Object> caseData) {
        return asList(
            coverLetterDocument(caseData),
            COSTS_ORDER_DOCUMENT_TYPE
        );
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails) {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        if (isCoRespondentDigital(caseDetails.getCaseData())) {
            addGenericUpdateNotificationEmailTask(tasks, caseDetails);
        } else {
            addCoRespondentPaperTasks(tasks, caseDetails);
        }
        return tasks.toArray(new Task[0]);
    }

    private void addCoRespondentPaperTasks(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        log.info("For case {} co-respondent uses traditional letters", caseDetails.getCaseId());

        if (isPaperUpdateEnabled()) {
            log.info("Features.PAPER_UPDATE = on.");

            if (isCostsClaimGranted(caseDetails.getCaseData())) {
                log.info("CaseID: {} - Cost claim granted", caseDetails.getCaseId());

                if (isCoRespondentRepresented(caseDetails.getCaseData())) {
                    log.info("CaseID: {} - Send Cost Order Cover Letter to CoRespondent Solicitor", caseDetails.getCaseId());
                    tasks.add(costOrderCoRespondentSolicitorCoverLetterGenerationTask);

                } else {
                    log.info("CaseID: {} - Send Cost Order Cover Letter to CoRespondent", caseDetails.getCaseId());
                    tasks.add(costOrderCoRespondentCoverLetterGenerationTask);

                }
                log.info("CaseID: {} - Documents sent to bulk print", caseDetails.getCaseId());
                tasks.add(fetchPrintDocsFromDmStore);
                tasks.add(bulkPrinterTask);

            } else {
                log.info("CaseID: {} - Cost claim not granted. Nothing was sent to bulk print", caseDetails.getCaseId());
            }
        } else {
            log.info("Features.PAPER_UPDATE = off. Nothing was sent to bulk print");
        }
    }

    private void addGenericUpdateNotificationEmailTask(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        log.info("For case {} co-respondent uses digital contact", caseDetails.getCaseId());
        tasks.add(sendPetitionerGenericUpdateNotificationEmailTask);
        tasks.add(sendRespondentGenericUpdateNotificationEmailTask);

        if (isCoRespondentLiableForCosts(caseDetails.getCaseData())) {
            log.info("CaseID: {} - corespondent is liable for costs. Email sent", caseDetails.getCaseId());
            tasks.add(sendCoRespondentGenericUpdateNotificationEmailTask);
        } else {
            log.info("CaseID: {} - corespondent is not liable for costs. Email not sent", caseDetails.getCaseId());
        }
    }

    private boolean isPaperUpdateEnabled() {
        return featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE);
    }

    private String coverLetterDocument(Map<String, Object> caseData) {
        return isCoRespondentRepresented(caseData)
            ? costOrderCoRespondentSolicitorCoverLetterGenerationTask.getDocumentType()
            : costOrderCoRespondentCoverLetterGenerationTask.getDocumentType();
    }
}
