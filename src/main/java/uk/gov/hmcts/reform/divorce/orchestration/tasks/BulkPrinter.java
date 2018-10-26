package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.FeatureToggleServiceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;

@Service
@Slf4j
public class BulkPrinter implements Task<Map<String, Object>> {

    private static final String XEROX_TYPE_PARAMETER = "CMC001";

    private static final String DOCUMENTS_GENERATED = "DocumentsGenerated";

    private static final String LETTER_TYPE = "letterType";

    private static final String ADDITIONAL_DATA_LETTER_TYPE_VALUE = "first-contact-pack";

    private static final String ADDITIONAL_DATA_CASE_IDENTIFIER_KEY = "caseIdentifier";

    private static final String ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";

    private static final String DATA_APPLICATION_PDF_BASE_64 = "data:application/pdf;base64,";

    private final SendLetterApi sendLetterApi;

    private AuthTokenGenerator authTokenGenerator;

    private FeatureToggleServiceClient featureToggleServiceClient;

    @Value("${feature-toggle.toggle.bulk-printer-toggle-name}")
    private String bulkPrintFeatureToggleName;

    @Autowired
    public BulkPrinter(AuthTokenGenerator authTokenGenerator , SendLetterApi sendLetterApi,
                       FeatureToggleServiceClient featureToggleServiceClient) {
        this.authTokenGenerator = authTokenGenerator;
        this.sendLetterApi = sendLetterApi;
        this.featureToggleServiceClient = featureToggleServiceClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {

        if (isBulkPrintToggleEnabled()) {

            CaseDetails caseDetails = (CaseDetails) context.getTransientObject(CASE_DETAILS_JSON_KEY);
            Map<String, GeneratedDocumentInfo> generatedDocumentInfoList =
                (Map<String, GeneratedDocumentInfo>) context.getTransientObject(DOCUMENTS_GENERATED);
            String miniPetition = convertToBase64String(generatedDocumentInfoList.get(DOCUMENT_TYPE_PETITION)
                .getBytes());
            String aosLetter = convertToBase64String(generatedDocumentInfoList.get(DOCUMENT_TYPE_INVITATION)
                .getBytes());
            sendToBulkPrint(context, caseDetails, miniPetition, aosLetter);
        } else {
            log.info(" Bulk print feature is toggled-off from feature toggle service");
        }
        return payload;
    }

    private boolean isBulkPrintToggleEnabled() {
        return
            Optional.ofNullable(featureToggleServiceClient.getToggle(bulkPrintFeatureToggleName).getEnable())
            .map(Boolean::valueOf).orElse(false);
    }

    private void sendToBulkPrint(TaskContext context, CaseDetails caseDetails, String miniPetition, String aosLetter) {
        try {

            SendLetterResponse sendLetterResponse = sendLetterApi.sendLetter(authTokenGenerator.generate(),
                new LetterWithPdfsRequest(Arrays.asList(aosLetter, miniPetition), XEROX_TYPE_PARAMETER,
                    wrapInMap(caseDetails)));
            log.info("Letter service produced the following letter Id {} for a case {}",
                    sendLetterResponse.letterId, caseDetails.getCaseId());
        } catch (Exception e) {
            context.setTaskFailed(true);
            log.error(e.getMessage());
            context.setTransientObject(this.getClass().getName() + "_Error", "Bulk print failed");
        }
    }

    private static Map<String, Object> wrapInMap(CaseDetails caseDetails) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(LETTER_TYPE, ADDITIONAL_DATA_LETTER_TYPE_VALUE);
        additionalData.put(ADDITIONAL_DATA_CASE_IDENTIFIER_KEY, caseDetails.getCaseId());
        additionalData.put(ADDITIONAL_DATA_CASE_REFERENCE_NUMBER_KEY, caseDetails.getCaseId());
        return additionalData;
    }

    private String convertToBase64String(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        sb.append(DATA_APPLICATION_PDF_BASE_64);
        sb.append(new String(Base64.encodeBase64(bytes, false)));
        return sb.toString();
    }
}