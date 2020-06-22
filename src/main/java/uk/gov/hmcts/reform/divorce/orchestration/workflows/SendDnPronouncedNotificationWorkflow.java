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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DnGrantedRespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DnGrantedRespondentSolicitorCoverLetterGenerationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COST_ORDER_OFFLINE_PACK_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.isCostsClaimGranted;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.BULK_PRINT_LETTER_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.DOCUMENT_TYPES_TO_PRINT;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.removeDocumentsByDocumentType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentDigital;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentLiableForCosts;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentRepresented;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentDigital;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendDnPronouncedNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendPetitionerGenericUpdateNotificationEmailTask sendPetitionerGenericUpdateNotificationEmailTask;
    private final SendRespondentGenericUpdateNotificationEmailTask sendRespondentGenericUpdateNotificationEmailTask;
    private final SendCoRespondentGenericUpdateNotificationEmailTask sendCoRespondentGenericUpdateNotificationEmailTask;

    private final CostOrderCoRespondentCoverLetterGenerationTask costOrderCoRespondentCoverLetterGenerationTask;
    private final CostOrderCoRespondentSolicitorCoverLetterGenerationTask costOrderCoRespondentSolicitorCoverLetterGenerationTask;

    private final DnGrantedRespondentCoverLetterGenerationTask dnGrantedRespondentCoverLetterGenerationTask;
    private final DnGrantedRespondentSolicitorCoverLetterGenerationTask dnGrantedRespondentSolicitorCoverLetterGenerationTask;

    private final FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;
    private final BulkPrinterTask bulkPrinterTask;

    private final FeatureToggleService featureToggleService;

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = caseDetails.getCaseData();
        final String coverLetterDocType = getCoverLetterDocumentType(caseData);

        Map<String, Object> returnCaseData = this.execute(
            getTasks(caseDetails),
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(BULK_PRINT_LETTER_TYPE, COST_ORDER_OFFLINE_PACK_CO_RESPONDENT),
            ImmutablePair.of(DOCUMENT_TYPES_TO_PRINT, getDocumentTypesToPrint(coverLetterDocType, caseId))
        );

        return removeDocumentsByDocumentType(returnCaseData, coverLetterDocType);
    }

    private List<String> getDocumentTypesToPrint(String coverLetterDocumentType, String caseId) {
        List<String> documentTypesToPrint = asList(coverLetterDocumentType, COSTS_ORDER_DOCUMENT_TYPE);

        log.info("Case {} has {} documents to print: {}", caseId, documentTypesToPrint.size(), documentTypesToPrint);

        return documentTypesToPrint;
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails) {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        addTasksForPetitioner(tasks, caseDetails);
        addTasksForRespondent(tasks, caseDetails);
        addTasksToCoRespondent(tasks, caseDetails);

        return tasks.toArray(new Task[0]);
    }

    private void addTasksForRespondent(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        final String caseId = caseDetails.getCaseId();

        if (isRespondentDigital(caseDetails.getCaseData())) {
            addGenericUpdateEmailToRespondentTask(tasks, caseDetails);
        } else {
            if (isPaperUpdateEnabled()) {
                log.info("Features.PAPER_UPDATE = on. Case: {}", caseId);

                addRespondentPaperTasks(tasks, caseDetails);
                // we should send all to bulk printing here
                // but not using this task, but like this:
                // uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoRespondentAosPackPrinterTask
                // this ^ task uses method: bulkPrinter.printSpecifiedDocument
                addByBulkPrintTasks(tasks, caseDetails);
            } else {
                log.info("Features.PAPER_UPDATE = off. Nothing will be sent to bulk print. Case: {}", caseId);
            }
        }
    }

    private void addTasksToCoRespondent(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        final String caseId = caseDetails.getCaseId();

        // shouldn't we check if co-resp exist? Jeremy

        if (isCoRespondentDigital(caseDetails.getCaseData())) {
            addGenericUpdateEmailToCoRespondentTask(tasks, caseDetails);
        } else {
            if (isPaperUpdateEnabled()) {
                log.info("Features.PAPER_UPDATE = on. Case: {}", caseId);

                addCoRespondentPaperTasks(tasks, caseDetails);
                // we should send all to bulk printing here
                // but not using this task, but like this:
                // uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoRespondentAosPackPrinterTask
                // this ^ task uses method: bulkPrinter.printSpecifiedDocument
                addByBulkPrintTasks(tasks, caseDetails);
            } else {
                log.info("Features.PAPER_UPDATE = off. Nothing will be sent to bulk print. Case: {}", caseId);
            }
        }
    }

    private void addTasksForPetitioner(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        log.info(
            "CaseID: {} - Adding task to send generic update (dn pronounced) to petitioner",
            caseDetails.getCaseId()
        );
        tasks.add(sendPetitionerGenericUpdateNotificationEmailTask);
    }

    private void addByBulkPrintTasks(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        log.info("CaseID: {} - Adding tasks to send to bulk print", caseDetails.getCaseId());
        tasks.add(fetchPrintDocsFromDmStore);
        tasks.add(bulkPrinterTask);
    }

    private void addRespondentPaperTasks(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        String caseId = caseDetails.getCaseId();
        log.info("For case {} respondent uses traditional letters", caseId);

        if (isRespondentRepresented(caseDetails.getCaseData())) {
            log.info("CaseID: {} - Adding task to send DN-Granted Cover Letter to Respondent Solicitor", caseId);
            tasks.add(dnGrantedRespondentCoverLetterGenerationTask);
        } else {
            log.info("CaseID: {} - Adding task to send DN-Granted Cover Letter to Respondent", caseId);
            tasks.add(dnGrantedRespondentSolicitorCoverLetterGenerationTask);
        }
    }

    private void addCoRespondentPaperTasks(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        String caseId = caseDetails.getCaseId();
        log.info("For case {} co-respondent uses traditional letters", caseId);

        if (isCostsClaimGranted(caseDetails.getCaseData())) {
            log.info("CaseID: {} - Cost claim granted", caseDetails.getCaseId());

            if (isCoRespondentRepresented(caseDetails.getCaseData())) {
                log.info("CaseID: {} - Adding task to send Cost Order Cover Letter to CoRespondent Solicitor", caseId);
                tasks.add(costOrderCoRespondentSolicitorCoverLetterGenerationTask);
            } else {
                log.info("CaseID: {} - Adding task to send Cost Order Cover Letter to CoRespondent", caseId);
                tasks.add(costOrderCoRespondentCoverLetterGenerationTask);
            }
        } else {
            log.info("CaseID: {} - Cost claim not granted. Nothing will be sent to bulk print", caseId);
        }
    }

    private void addGenericUpdateEmailToCoRespondentTask(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        String caseId = caseDetails.getCaseId();
        log.info("For case {} co-respondent uses digital contact", caseDetails.getCaseId());

        if (isCoRespondentLiableForCosts(caseDetails.getCaseData())) {
            log.info("CaseID: {} - corespondent is liable for costs. Added task to send email do co-resp", caseId);
            tasks.add(sendCoRespondentGenericUpdateNotificationEmailTask);
        } else {
            log.info("CaseID: {} - corespondent is not liable for costs. Email will not be sent", caseId);
        }
    }

    private void addGenericUpdateEmailToRespondentTask(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        log.info("For case {} respondent uses digital contact", caseDetails.getCaseId());
        tasks.add(sendRespondentGenericUpdateNotificationEmailTask);
    }

    private boolean isPaperUpdateEnabled() {
        return featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE);
    }

    private String getCoverLetterDocumentType(Map<String, Object> caseData) {
        return isCoRespondentRepresented(caseData)
            ? costOrderCoRespondentSolicitorCoverLetterGenerationTask.getDocumentType()
            : costOrderCoRespondentCoverLetterGenerationTask.getDocumentType();
    }
}
