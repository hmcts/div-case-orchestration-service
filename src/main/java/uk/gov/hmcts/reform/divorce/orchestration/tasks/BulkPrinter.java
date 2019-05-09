package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Base64.getEncoder;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_INVITATION;

@Service
@Slf4j
public class BulkPrinter implements Task<Map<String, Object>> {

    private static final String XEROX_TYPE_PARAMETER = "DIV001";
    private static final String DOCUMENTS_GENERATED = "DocumentsGenerated";
    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";
    private static final String LETTER_TYPE_RESPONDENT_PACK = "respondent-aos-pack";
    private static final String LETTER_TYPE_CO_RESPONDENT_PACK = "co-respondent-aos-pack";

    private final SendLetterApi sendLetterApi;

    private final AuthTokenGenerator authTokenGenerator;

    private final FeatureToggleServiceClient featureToggleServiceClient;

    @Value("${feature-toggle.toggle.bulk-printer-toggle-name}")
    private String bulkPrintFeatureToggleName;

    @Autowired
    public BulkPrinter(AuthTokenGenerator authTokenGenerator, SendLetterApi sendLetterApi, FeatureToggleServiceClient featureToggleServiceClient) {
        this.authTokenGenerator = authTokenGenerator;
        this.sendLetterApi = sendLetterApi;
        this.featureToggleServiceClient = featureToggleServiceClient;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {

        if (isBulkPrintToggleEnabled()) {

            CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);
            Map<String, GeneratedDocumentInfo> generatedDocumentInfoList = context.getTransientObject(DOCUMENTS_GENERATED);
            String miniPetition = getEncoder().encodeToString(generatedDocumentInfoList.get(DOCUMENT_TYPE_PETITION)
                .getBytes());
            String aosLetter = getEncoder().encodeToString(generatedDocumentInfoList.get(DOCUMENT_TYPE_RESPONDENT_INVITATION)
                .getBytes());

            // one time generation of auth token.
            final String authToken = authTokenGenerator.generate();

            sendRespondentPack(context, authToken, caseDetails, miniPetition, aosLetter);

            final GeneratedDocumentInfo coRespondentLetter = generatedDocumentInfoList.get(DOCUMENT_TYPE_CO_RESPONDENT_INVITATION);
            if (coRespondentLetter != null) {
                // Co-respondent letter only exists if there is a named co-respondent in an CTSC adultery case.
                sendCoRespondentPack(context, authToken, caseDetails, getEncoder().encodeToString(coRespondentLetter.getBytes()), miniPetition);
            }

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

    private void sendRespondentPack(final TaskContext context, final String authToken, final CaseDetails caseDetails, final String miniPetition,
                                    final String aosLetter) {
        try {
            log.info("Sending respondent pack for case {}", caseDetails.getCaseId());
            SendLetterResponse sendLetterResponse = sendLetterApi.sendLetter(authToken,
                new LetterWithPdfsRequest(asList(aosLetter, miniPetition), XEROX_TYPE_PARAMETER, getAdditionalData(caseDetails,
                    LETTER_TYPE_RESPONDENT_PACK)));
            // The order of aosLetter and miniPetition arguments is important here.
            // Sending the aosLetter first ensures it is the first piece of paper in the envelope so that the address label is displayed.

            log.info("Letter service produced the following letter Id {} for case {}", sendLetterResponse.letterId, caseDetails.getCaseId());
        } catch (Exception e) {
            context.setTaskFailed(true);
            log.error("Respondent pack bulk print failed for case {}", caseDetails.getCaseId(), e);
            context.setTransientObject(this.getClass().getName() + "_Error", "Bulk print failed for respondent pack");
        }
    }

    private void sendCoRespondentPack(final TaskContext context, final String authToken, final CaseDetails caseDetails,
                                      final String coRespondentLetter, final String miniPetition) {
        try {
            log.info("Sending co-respondent pack for case {}", caseDetails.getCaseId());
            SendLetterResponse sendLetterResponse = sendLetterApi.sendLetter(authToken,
                new LetterWithPdfsRequest(asList(coRespondentLetter, miniPetition), XEROX_TYPE_PARAMETER, getAdditionalData(caseDetails,
                    LETTER_TYPE_CO_RESPONDENT_PACK)));
            // The order of coRespondentLetter and miniPetition arguments is important here.
            // Sending the coRespondentLetter first ensures it is the first piece of paper in the envelope so that the address label is displayed.

            log.info("Letter service produced the following letter Id {} for case {}", sendLetterResponse.letterId, caseDetails.getCaseId());
        } catch (final Exception e) {
            context.setTaskFailed(true);
            log.error(String.format("Co-respondent pack bulk print failed for case [%s]", caseDetails.getCaseId()), e);
            context.setTransientObject(this.getClass().getName() + "_Error", "Bulk print failed for co-respondent pack");
        }
    }

    private static Map<String, Object> getAdditionalData(final CaseDetails caseDetails, final String letterType) {
        Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(LETTER_TYPE_KEY, letterType);
        additionalData.put(CASE_IDENTIFIER_KEY, caseDetails.getCaseId());
        additionalData.put(CASE_REFERENCE_NUMBER_KEY, caseDetails.getCaseId());
        return additionalData;
    }
}
