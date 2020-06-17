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
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerCoENotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentCoENotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoECoRespondentCoverLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoERespondentLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CoERespondentSolicitorLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COE_OFFLINE_PACK_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.BULK_PRINT_LETTER_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.DOCUMENT_TYPES_TO_PRINT;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.removeDocumentsByDocumentType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;

@Component
@Slf4j
@RequiredArgsConstructor
public class CaseLinkedForHearingWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendPetitionerCoENotificationEmailTask sendPetitionerCoENotificationEmailTask;
    private final SendRespondentCoENotificationEmailTask sendRespondentCoENotificationEmailTask;
    private final SendCoRespondentGenericUpdateNotificationEmailTask sendCoRespondentGenericUpdateNotificationEmailTask;

    private final CoERespondentLetterGenerationTask coERespondentLetterGenerationTask;
    private final CoERespondentSolicitorLetterGenerationTask coERespondentSolicitorLetterGenerationTask;
    private final CoECoRespondentCoverLetterGenerationTask coECoRespondentCoverLetterGenerationTask;
    private final FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;
    private final BulkPrinterTask bulkPrinterTask;

    private final FeatureToggleService featureToggleService;

    private final CaseDataUtils caseDataUtils;

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        log.info("Running CaseLinkedForHearingWorkflow for case id {}.", caseDetails.getCaseId());

        Map<String, Object> caseDataToReturn = this.execute(
            getTasks(caseDetails),
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId()),
            ImmutablePair.of(BULK_PRINT_LETTER_TYPE, COE_OFFLINE_PACK_RESPONDENT),
            ImmutablePair.of(DOCUMENT_TYPES_TO_PRINT, getDocumentTypesToPrint(caseDetails.getCaseData()))
        );

        return removeCoverLettersFrom(caseDataToReturn);
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails) {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();
        Map<String, Object> caseData = caseDetails.getCaseData();
        boolean oneOrMorePartyUsesPaperUpdates = false;

        tasks.add(sendPetitionerCoENotificationEmailTask);

        if (isRespondentUsingDigitalContact(caseData)) {
            log.info("For case {} respondent uses digital contact", caseDetails.getCaseId());
            tasks.add(sendRespondentCoENotificationEmailTask);
        } else {
            log.info("For case {} respondent uses traditional letters", caseDetails.getCaseId());
            oneOrMorePartyUsesPaperUpdates = true;
            if (featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)) {
                if (isRespondentRepresented(caseData)) {
                    log.info("For case {} respondent is represented by a solicitor", caseDetails.getCaseId());
                    tasks.add(coERespondentSolicitorLetterGenerationTask);
                } else {
                    log.info("For case {} respondent is not represented by a solicitor", caseDetails.getCaseId());
                    tasks.add(coERespondentLetterGenerationTask);
                }
            }
        }

        if (caseDataUtils.isAdulteryCaseWithNamedCoRespondent(caseData)) {
            if (isCoRespContactMethodDigital(caseData)) {
                log.info("For case {} co-respondent uses digital contact", caseDetails.getCaseId());
                tasks.add(sendCoRespondentGenericUpdateNotificationEmailTask);
            } else {
                log.info("For case {} co-respondent uses traditional letters", caseDetails.getCaseId());
                oneOrMorePartyUsesPaperUpdates = true;
                if (featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)) {
                    tasks.add(coECoRespondentCoverLetterGenerationTask);
                }
            }
        }

        if (oneOrMorePartyUsesPaperUpdates) {
            if (featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)) {
                log.info("Features.PAPER_UPDATE = on.");
                tasks.add(fetchPrintDocsFromDmStore);
                tasks.add(bulkPrinterTask);
            } else {
                log.info("Features.PAPER_UPDATE = off. Nothing was sent to bulk print. Defaulting to email notifications.");
                tasks = getEmailNotificationTasks(caseData);
            }
        }

        return tasks.toArray(new Task[0]);
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

    private Map<String, Object> removeCoverLettersFrom(Map<String, Object> caseDataToReturn) {
        return removeDocumentsByDocumentType(caseDataToReturn,
            coERespondentLetterGenerationTask.getDocumentType(),
            coECoRespondentCoverLetterGenerationTask.getDocumentType());
    }

    private List<String> getDocumentTypesToPrint(Map<String, Object> caseData) {
        List<String> documentTypes = new ArrayList<>(asList(
            coERespondentLetterGenerationTask.getDocumentType(),
            DOCUMENT_TYPE_COE
        ));
        if (caseDataUtils.isAdulteryCaseWithNamedCoRespondent(caseData)) {
            documentTypes.add(coECoRespondentCoverLetterGenerationTask.getDocumentType());
        }
        return documentTypes;
    }

    private boolean isCoRespContactMethodDigital(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase((String) caseData.get(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL));
    }

    private boolean isRespondentUsingDigitalContact(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty((String) caseData.get(RESP_IS_USING_DIGITAL_CHANNEL)));
    }
}
