package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.BulkPrintConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStoreTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCoRespondentGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerCoENotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentCoENotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoECoRespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoERespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoERespondentSolicitorLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.MultiBulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.MultiBulkPrinterTask.ContextFields.MULTI_BULK_PRINT_CONFIGS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.removeDocumentsByDocumentType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentDigital;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentDigital;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseLinkedForHearingWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendPetitionerCoENotificationEmailTask sendPetitionerCoENotificationEmailTask;
    private final SendRespondentCoENotificationEmailTask sendRespondentCoENotificationEmailTask;
    private final SendCoRespondentGenericUpdateNotificationEmailTask sendCoRespondentGenericUpdateNotificationEmailTask;

    private final CoERespondentCoverLetterGenerationTask coERespondentCoverLetterGenerationTask;
    private final CoERespondentSolicitorLetterGenerationTask coERespondentSolicitorLetterGenerationTask;
    private final CoECoRespondentCoverLetterGenerationTask coECoRespondentCoverLetterGenerationTask;

    private final FetchPrintDocsFromDmStoreTask fetchPrintDocsFromDmStoreTask;
    private final MultiBulkPrinterTask multiBulkPrinterTask;

    private final FeatureToggleService featureToggleService;
    private final CaseDataUtils caseDataUtils;

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        String caseId = caseDetails.getCaseId();
        log.info("CaseID: {} - Running CaseLinkedForHearingWorkflow.", caseId);

        Task<Map<String, Object>>[] tasks = getTasks(caseDetails);
        List<BulkPrintConfig> configs = getBulkPrintConfigForMultiPrinting(caseDetails);

        Map<String, Object> caseDataToReturn = this.execute(
            tasks,
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(MULTI_BULK_PRINT_CONFIGS, configs)
        );

        log.info("CaseID: {} CaseLinkedForHearingWorkflow executed.", caseId);

        return removeCoverLettersFrom(caseDataToReturn);
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails) {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();
        Map<String, Object> caseData = caseDetails.getCaseData();
        boolean oneOrMorePartyUsesPaperUpdates = false;

        tasks.add(sendPetitionerCoENotificationEmailTask);

        if (isRespondentDigital(caseData)) {
            addRespondentNotificationEmailTask(tasks, caseDetails);
        } else {
            oneOrMorePartyUsesPaperUpdates = true;
            addRespondentNotificationLetterTask(tasks, caseDetails);
        }

        if (caseDataUtils.isAdulteryCaseWithNamedCoRespondent(caseData)) {
            if (isCoRespondentDigital(caseData)) {
                addCoRespondentNotificationEmailTask(tasks, caseDetails);
            } else {
                oneOrMorePartyUsesPaperUpdates = true;
                addCoRespondentNotificationLetterTask(tasks, caseDetails);
            }
        }

        if (oneOrMorePartyUsesPaperUpdates) {
            if (isPaperUpdateEnabled()) {
                log.info("Features.PAPER_UPDATE = on. Add tasks to send documents to bulk print");
                tasks.add(fetchPrintDocsFromDmStoreTask);
                tasks.add(multiBulkPrinterTask);
            } else {
                log.info("Features.PAPER_UPDATE = off. Nothing will be sent to bulk print. Defaulting to email.");
                tasks = getEmailNotificationTasks(caseData);
            }
        }

        return tasks.toArray(new Task[0]);
    }

    public List<BulkPrintConfig> getBulkPrintConfigForMultiPrinting(CaseDetails caseDetails) {
        final String caseId = caseDetails.getCaseId();
        final Map<String, Object> caseData = caseDetails.getCaseData();
        final List<String> respondentDocTypes = new ArrayList<>();
        final List<String> coRespondentDocTypes = new ArrayList<>();

        if (isPaperUpdateEnabled()) {
            if (!isRespondentDigital(caseData)) {
                addRespondentCoverLetterDocumentType(respondentDocTypes, caseDetails);
            } else {
                log.info("CaseID: {} respondent is using digital channel. No documents to print", caseId);
            }

            if (caseDataUtils.isAdulteryCaseWithNamedCoRespondent(caseData)) {
                log.info("CaseID: {} named co-respondent", caseId);

                if (!isCoRespondentDigital(caseData)) {
                    addCoRespondentCoverLetterDocumentType(coRespondentDocTypes, caseDetails);
                } else {
                    log.info("CaseID: {} co-respondent is using digital channel. No documents to print", caseId);
                }
            } else {
                log.info("CaseID: {} there is no co-respondent", caseId);
            }
        } else {
            log.info("Features.PAPER_UPDATE = off. No documents will be sent to print");
        }

        List<BulkPrintConfig> config = new ArrayList<>();
        if (!respondentDocTypes.isEmpty()) {
            config.add(new BulkPrintConfig(
                isRespondentRepresented(caseData)
                    ? coERespondentSolicitorLetterGenerationTask.getDocumentType()
                    : coERespondentCoverLetterGenerationTask.getDocumentType(),
                respondentDocTypes
            ));
        }

        if (!coRespondentDocTypes.isEmpty()) {
            config.add(
                new BulkPrintConfig(coECoRespondentCoverLetterGenerationTask.getDocumentType(), coRespondentDocTypes)
            );
        }

        log.info("CaseID: {} multi bulk print config: {}", caseId, config);

        return config;
    }

    private void addRespondentNotificationEmailTask(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        log.info("CaseID: {} respondent uses digital contact", caseDetails.getCaseId());
        tasks.add(sendRespondentCoENotificationEmailTask);
    }

    private void addRespondentNotificationLetterTask(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        log.info("CaseID: {} respondent uses traditional letters", caseDetails.getCaseId());
        if (isPaperUpdateEnabled()) {
            if (isRespondentRepresented(caseDetails.getCaseData())) {
                log.info("CaseID: {} respondent is represented by a solicitor", caseDetails.getCaseId());
                tasks.add(coERespondentSolicitorLetterGenerationTask);
            } else {
                log.info("CaseID: {} respondent is not represented by a solicitor", caseDetails.getCaseId());
                tasks.add(coERespondentCoverLetterGenerationTask);
            }
        }
    }

    private void addCoRespondentNotificationEmailTask(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        log.info("CaseID: {} co-respondent uses digital contact", caseDetails.getCaseId());
        tasks.add(sendCoRespondentGenericUpdateNotificationEmailTask);
    }

    private void addCoRespondentNotificationLetterTask(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        log.info("CaseID: {} co-respondent uses traditional letters", caseDetails.getCaseId());
        if (isPaperUpdateEnabled()) {
            tasks.add(coECoRespondentCoverLetterGenerationTask);
        }
    }

    private void addRespondentCoverLetterDocumentType(List<String> documentTypes, CaseDetails caseDetails) {
        if (isRespondentRepresented(caseDetails.getCaseData())) {
            log.info("CaseID: {} add cover letter to print for the respondent's solicitor", caseDetails.getCaseId());
            documentTypes.add(coERespondentSolicitorLetterGenerationTask.getDocumentType());
        } else {
            log.info("CaseID: {} add cover letter to print for the respondent", caseDetails.getCaseId());
            documentTypes.add(coERespondentCoverLetterGenerationTask.getDocumentType());
        }

        log.info("CaseID: {} add CoE to print (respondent/sol)", caseDetails.getCaseId());
        documentTypes.add(DOCUMENT_TYPE_COE);
    }

    private void addCoRespondentCoverLetterDocumentType(List<String> documentTypes, CaseDetails caseDetails) {
        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} has cover letters to print for the co-respondent (or their solicitor)", caseId);
        documentTypes.add(coECoRespondentCoverLetterGenerationTask.getDocumentType());
        log.info("CaseID: {} add CoE to print (co-respondent/sol)", caseId);
        documentTypes.add(DOCUMENT_TYPE_COE);
    }

    private List<Task<Map<String, Object>>> getEmailNotificationTasks(Map<String, Object> caseData) {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        tasks.add(sendPetitionerCoENotificationEmailTask);
        tasks.add(sendRespondentCoENotificationEmailTask);

        if (caseDataUtils.isAdulteryCaseWithNamedCoRespondent(caseData)) {
            tasks.add(sendCoRespondentGenericUpdateNotificationEmailTask);
        }

        return tasks;
    }

    private boolean isPaperUpdateEnabled() {
        return featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE);
    }

    private Map<String, Object> removeCoverLettersFrom(Map<String, Object> caseDataToReturn) {
        return removeDocumentsByDocumentType(caseDataToReturn,
            coERespondentCoverLetterGenerationTask.getDocumentType(),
            coERespondentSolicitorLetterGenerationTask.getDocumentType(),
            coECoRespondentCoverLetterGenerationTask.getDocumentType()
        );
    }
}
