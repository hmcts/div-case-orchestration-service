package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.CaseFormatterAddDocuments;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.FetchPrintDocsFromDmStore;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendCoRespondentGenericUpdateNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendPetitionerCertificateOfEntitlementNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SendRespondentCertificateOfEntitlementNotificationEmailTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.CertificateOfEntitlementLetterGenerationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Strings.nullToEmpty;
import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_LETTER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_OFFLINE_PACK_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_OFFLINE_PACK_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_IS_USING_DIGITAL_CHANNEL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.BULK_PRINT_LETTER_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BulkPrinterTask.DOCUMENT_TYPES_TO_PRINT;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.removeDocumentByDocumentType;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;

@Component
@Slf4j
public class CaseLinkedForHearingWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Autowired
    private SendPetitionerCertificateOfEntitlementNotificationEmailTask sendPetitionerCertificateOfEntitlementNotificationEmailTask;

    @Autowired
    private SendRespondentCertificateOfEntitlementNotificationEmailTask sendRespondentCertificateOfEntitlementNotificationEmailTask;

    @Autowired
    SendCoRespondentGenericUpdateNotificationEmailTask sendCoRespondentGenericUpdateNotificationEmailTask;

    @Autowired
    private CertificateOfEntitlementLetterGenerationTask certificateOfEntitlementLetterGenerationTask;

    @Autowired
    private CaseFormatterAddDocuments caseFormatterAddDocuments;

    @Autowired
    private FetchPrintDocsFromDmStore fetchPrintDocsFromDmStore;

    @Autowired
    private BulkPrinterTask bulkPrinterTask;

    @Autowired
    private FeatureToggleService featureToggleService;

    public Map<String, Object> run(CaseDetails caseDetails, String authToken) throws WorkflowException {
        Map<String, Object> incomingCaseData = caseDetails.getCaseData();

        Map<String, Object> caseDataToReturn = this.execute(
            getTasks(incomingCaseData),
            incomingCaseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(CASE_ID_JSON_KEY, caseDetails.getCaseId()),
            ImmutablePair.of(BULK_PRINT_LETTER_TYPE, getBulkPrintLetterType(incomingCaseData)),
            ImmutablePair.of(DOCUMENT_TYPES_TO_PRINT, getDocumentTypesToPrint())
        );

        log.info("Running CaseLinkedForHearingWorkflow for case id {}.", caseDetails.getCaseId());

        return removeDocumentByDocumentType(caseDataToReturn, certificateOfEntitlementLetterGenerationTask.getDocumentType());
    }

    private List<String> getDocumentTypesToPrint() {
        return asList(
            CERTIFICATE_OF_ENTITLEMENT_LETTER_DOCUMENT_TYPE,
            CERTIFICATE_OF_ENTITLEMENT_DOCUMENT_TYPE
        );
    }

    private Task<Map<String, Object>>[] getTasks(Map<String, Object> caseData) {
        List<Task<Map<String, Object>>> tasks = new ArrayList<>();
        if (isRespondentUsingDigitalContact(caseData)) {
            tasks.add(sendPetitionerCertificateOfEntitlementNotificationEmailTask);
            tasks.add(sendRespondentCertificateOfEntitlementNotificationEmailTask);
            tasks.add(sendCoRespondentGenericUpdateNotificationEmailTask);
        } else {
            if (featureToggleService.isFeatureEnabled(Features.PAPER_UPDATE)) {
                tasks.add(certificateOfEntitlementLetterGenerationTask);
                tasks.add(caseFormatterAddDocuments);
                tasks.add(fetchPrintDocsFromDmStore);
                tasks.add(bulkPrinterTask);
            }
        }

        Task<Map<String, Object>>[] arr = new Task[tasks.size()];

        return tasks.toArray(arr);
    }

    private String getBulkPrintLetterType(Map<String, Object> caseData) {
        if (isRespondentRepresented(caseData)) {
            return CERTIFICATE_OF_ENTITLEMENT_OFFLINE_PACK_RESPONDENT_SOLICITOR;
        }
        return CERTIFICATE_OF_ENTITLEMENT_OFFLINE_PACK_RESPONDENT;
    }

    private boolean isRespondentUsingDigitalContact(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(nullToEmpty((String) caseData.get(RESP_IS_USING_DIGITAL_CHANNEL)));
    }
}
