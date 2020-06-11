package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CostOrderLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CostOrderNotificationLetterGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCoRespondentGenericUpdateNotificationEmail;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COST_ORDER_OFFLINE_PACK_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_BOTH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.WHO_PAYS_COSTS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.BULK_PRINT_LETTER_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.DOCUMENT_TYPES_TO_PRINT;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.removeDocumentByDocumentType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isCoRespondentRepresented;

@Component
@AllArgsConstructor
@Slf4j
public class SendDnPronouncedNotificationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SendPetitionerGenericUpdateNotificationEmailTask sendPetitionerGenericUpdateNotificationEmailTask;
    private final SendRespondentGenericUpdateNotificationEmailTask sendRespondentGenericUpdateNotificationEmailTask;
    private final SendCoRespondentGenericUpdateNotificationEmail sendCoRespondentGenericUpdateNotificationEmail;

    private final CostOrderLetterGenerationTask costOrderLetterGenerationTask;
    private final CostOrderNotificationLetterGenerationTask costOrderNotificationLetterGenerationTask;
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

        return removeDocumentByDocumentType(returnCaseData, documentToRemove(returnCaseData));
    }

    private List<String> getDocumentTypesToPrint(Map<String, Object> caseData) {
        return asList(
            documentToRemove(caseData),
            COSTS_ORDER_DOCUMENT_TYPE
        );
    }

    private Task<Map<String, Object>>[] getTasks(CaseDetails caseDetails) {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();
        Map<String, Object> caseData = caseDetails.getCaseData();

        if (isCoRespContactMethodIsDigital(caseData)) {
            tasks.add(sendPetitionerGenericUpdateNotificationEmailTask);
            tasks.add(sendRespondentGenericUpdateNotificationEmailTask);

            if (isCoRespondentLiableForCosts(caseData)) {
                tasks.add(sendCoRespondentGenericUpdateNotificationEmail);
            }
            log.info("For case {} co-respondent uses digital contact", caseDetails.getCaseId());
        } else {
            log.info("For case {} co-respondent uses traditional letters", caseDetails.getCaseId());
            if (isPaperUpdateEnabled()) {

                if (isCoRespondentRepresented(caseData)) {
                    tasks.add(costOrderNotificationLetterGenerationTask);
                } else {
                    tasks.add(costOrderLetterGenerationTask);
                }

                tasks.add(fetchPrintDocsFromDmStore);
                tasks.add(bulkPrinterTask);
            } else {
                log.info("Features.PAPER_UPDATE = off. Nothing was sent to bulk print");
            }
        }

        Task<Map<String, Object>>[] arr = new Task[tasks.size()];
        return tasks.toArray(arr);
    }

    private boolean isCoRespContactMethodIsDigital(Map<String, Object> caseData) { //TODO maybe move to PartyRepresentedChecker so can be tested?
        return YES_VALUE.equalsIgnoreCase(String.valueOf(caseData.get(CO_RESPONDENT_IS_USING_DIGITAL_CHANNEL)));
    }

    private boolean isCoRespondentLiableForCosts(Map<String, Object> caseData) { //TODO maybe move to PartyRepresentedChecker so can be tested?
        String whoPaysCosts = String.valueOf(caseData.get(WHO_PAYS_COSTS_CCD_FIELD));

        return WHO_PAYS_CCD_CODE_FOR_CO_RESPONDENT.equalsIgnoreCase(whoPaysCosts)
            || WHO_PAYS_CCD_CODE_FOR_BOTH.equalsIgnoreCase(whoPaysCosts);
    }

    private boolean isPaperUpdateEnabled() {
        return featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE);
    }

    private String documentToRemove(Map<String, Object> caseData) {
        return isCoRespondentRepresented(caseData)
            ? costOrderNotificationLetterGenerationTask.getDocumentType()
            : costOrderLetterGenerationTask.getDocumentType();
    }

}
