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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CostOrderCoRespondentSolicitorCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DnGrantedRespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.DnGrantedRespondentSolicitorCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.MultiBulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor.isCostsClaimGranted;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.MultiBulkPrinterTask.ContextFields.MULTI_BULK_PRINT_CONFIGS;
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

    private final FetchPrintDocsFromDmStoreTask fetchPrintDocsFromDmStoreTask;
    private final MultiBulkPrinterTask multiBulkPrinterTask;

    private final FeatureToggleService featureToggleService;
    private final CaseDataUtils caseDataUtils;

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = caseDetails.getCaseData();

        Map<String, Object> returnCaseData = this.execute(
            getTasks(caseDetails),
            caseData,
            ImmutablePair.of(CASE_ID_JSON_KEY, caseId),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(MULTI_BULK_PRINT_CONFIGS, getBulkPrintConfigForMultiPrinting(caseDetails))
        );

        return removeDocumentsByDocumentType(
            returnCaseData,
            respondentOrSolicitor(caseData).getDocumentType(),
            coRespondentOrSolicitor(caseData).getDocumentType()
        );
    }

    private List<BulkPrintConfig> getBulkPrintConfigForMultiPrinting(CaseDetails caseDetails) {
        final String caseId = caseDetails.getCaseId();
        final Map<String, Object> caseData = caseDetails.getCaseData();
        List<BulkPrintConfig> config = new ArrayList<>();

        if (!isPaperUpdateEnabled()) {
            log.info("Features.PAPER_UPDATE = off. Nothing will be sent to bulk print. Defaulting to email.");
            return Collections.emptyList();
        }

        if (!isRespondentDigital(caseData)) {
            config.add(createBulkPrintConfigForRespondentOrSolicitor(caseDetails));
        } else {
            log.info("CaseID: {} respondent is using digital channel. No documents to print", caseId);
        }

        if (caseDataUtils.isAdulteryCaseWithNamedCoRespondent(caseData)) {
            if (!isCoRespondentDigital(caseData)) {
                config.add(createBulkPrintConfigForCoRespondentOrSolicitor(caseDetails));
            } else {
                log.info("CaseID: {} co-respondent is using digital channel. No documents to print", caseId);
            }
        } else {
            log.info("CaseID: {} there is no named co-respondent", caseId);
        }

        log.info("CaseID: {} multi bulk print config: {}", caseId, config);

        return config;
    }

    private BulkPrintConfig createBulkPrintConfigForCoRespondentOrSolicitor(CaseDetails caseDetails) {
        final String caseId = caseDetails.getCaseId();
        final Map<String, Object> caseData = caseDetails.getCaseData();
        List<String> coRespondentDocTypes = new ArrayList<>();

        log.info("CaseID: {} named co-respondent", caseId);

        if (isCostsClaimGranted(caseData)) {
            log.info("CaseID: {} adding cost order cover letter doc for co-respondent (solicitor)", caseId);
            coRespondentDocTypes.add(coRespondentOrSolicitor(caseData).getDocumentType());

            log.info("CaseID: {} adding costOrder doc for respondent (solicitor)", caseId);
            coRespondentDocTypes.add(COSTS_ORDER_DOCUMENT_TYPE);
        } else {
            log.info("CaseID: {} no cost order document", caseId);
        }

        return new BulkPrintConfig(coRespondentOrSolicitor(caseData).getDocumentType(), coRespondentDocTypes);
    }

    private BulkPrintConfig createBulkPrintConfigForRespondentOrSolicitor(CaseDetails caseDetails) {
        final String caseId = caseDetails.getCaseId();
        final Map<String, Object> caseData = caseDetails.getCaseData();
        List<String> respondentDocTypes = new ArrayList<>();

        log.info("CaseID: {} adding dn granted cover letter doc for respondent (solicitor)", caseId);
        respondentDocTypes.add(isRespondentRepresented(caseData)
            ? dnGrantedRespondentSolicitorCoverLetterGenerationTask.getDocumentType()
            : dnGrantedRespondentCoverLetterGenerationTask.getDocumentType()
        );

        log.info("CaseID: {} adding dn granted certificate for respondent (solicitor)", caseId);
        respondentDocTypes.add(DECREE_NISI_DOCUMENT_TYPE);

        if (isCostsClaimGranted(caseData)) {
            log.info("CaseID: {} adding costOrder doc for respondent (solicitor)", caseId);
            respondentDocTypes.add(COSTS_ORDER_DOCUMENT_TYPE);
        } else {
            log.info("CaseID: {} no cost order document", caseId);
        }

        return new BulkPrintConfig(respondentOrSolicitor(caseData).getDocumentType(), respondentDocTypes);
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails) {
        final String caseId = caseDetails.getCaseId();
        final Map<String, Object> caseData = caseDetails.getCaseData();
        final boolean respondentDigital = isRespondentDigital(caseData);
        final boolean coRespondentDigital = isCoRespondentDigital(caseData);
        final boolean coRespondentExists = caseDataUtils.isAdulteryCaseWithNamedCoRespondent(caseData);
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();

        log.info("CaseID: {} adding task for email to petitioner and respondent", caseId);
        tasks.add(sendPetitionerGenericUpdateNotificationEmailTask);

        if (respondentDigital) {
            tasks.add(sendRespondentGenericUpdateNotificationEmailTask);
        } else {
            addRespondentPaperTasks(tasks, caseDetails);
        }

        if (coRespondentExists) {
            if (coRespondentDigital) {
                addEmailForCoRespondentTask(tasks, caseDetails);
            } else {
                addCoRespondentPaperTasks(tasks, caseDetails);
            }
        } else {
            log.info("CaseID: {} - there is no co-respondent named", caseId);
        }

        if ((coRespondentExists && !coRespondentDigital) || !respondentDigital) {
            log.info("CaseID: {} - Adding task to send to bulk print", caseId);
            tasks.add(fetchPrintDocsFromDmStoreTask);
            tasks.add(multiBulkPrinterTask);
        }

        return tasks.toArray(new Task[0]);
    }

    private void addRespondentPaperTasks(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        if (!isPaperUpdateEnabled()) {
            log.info("Features.PAPER_UPDATE = off. Nothing will be sent to bulk print");
        }

        Map<String, Object> caseData = caseDetails.getCaseData();
        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} - Adding task to send DN Granted Cover Letter to Respondent (solicitor)", caseId);
        tasks.add(respondentOrSolicitor(caseData));
    }

    private void addCoRespondentPaperTasks(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        log.info("For case {} co-respondent uses traditional letters", caseDetails.getCaseId());

        if (!isPaperUpdateEnabled()) {
            log.info("Features.PAPER_UPDATE = off. Nothing will be sent to bulk print");
        }

        if (isCostsClaimGranted(caseDetails.getCaseData())) {
            log.info("CaseID: {} - Cost claim granted", caseDetails.getCaseId());

            if (isCoRespondentRepresented(caseDetails.getCaseData())) {
                log.info("CaseID: {} - Adding task to send Cost Order Cover Letter to CoRespondent Solicitor", caseDetails.getCaseId());
                tasks.add(costOrderCoRespondentSolicitorCoverLetterGenerationTask);
            } else {
                log.info("CaseID: {} - Adding task to send Cost Order Cover Letter to CoRespondent", caseDetails.getCaseId());
                tasks.add(costOrderCoRespondentCoverLetterGenerationTask);
            }
        } else {
            log.info("CaseID: {} - Cost claim not granted. Nothing will be sent to bulk print", caseDetails.getCaseId());
        }
    }

    private void addEmailForCoRespondentTask(List<Task<Map<String, Object>>> tasks, CaseDetails caseDetails) {
        String caseId = caseDetails.getCaseId();

        log.info("CaseID: {} co-respondent uses digital contact", caseId);

        if (isCoRespondentLiableForCosts(caseDetails.getCaseData())) {
            log.info("CaseID: {} - corespondent is liable for costs. Adding task to send email to co-resp", caseId);
            tasks.add(sendCoRespondentGenericUpdateNotificationEmailTask);
        } else {
            log.info("CaseID: {} - corespondent is not liable for costs. Email will not be sent", caseId);
        }
    }

    private boolean isPaperUpdateEnabled() {
        return featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE);
    }

    private BasePayloadSpecificDocumentGenerationTask respondentOrSolicitor(Map<String, Object> caseData) {
        return isRespondentRepresented(caseData)
            ? dnGrantedRespondentSolicitorCoverLetterGenerationTask
            : dnGrantedRespondentCoverLetterGenerationTask;
    }

    private BasePayloadSpecificDocumentGenerationTask coRespondentOrSolicitor(Map<String, Object> caseData) {
        return isCoRespondentRepresented(caseData)
            ? costOrderCoRespondentSolicitorCoverLetterGenerationTask
            : costOrderCoRespondentCoverLetterGenerationTask;
    }
}
