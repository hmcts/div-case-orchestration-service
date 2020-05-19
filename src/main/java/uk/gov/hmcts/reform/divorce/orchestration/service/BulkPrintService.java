package uk.gov.hmcts.reform.divorce.orchestration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.sendletter.api.LetterWithPdfsRequest;
import uk.gov.hmcts.reform.sendletter.api.SendLetterApi;
import uk.gov.hmcts.reform.sendletter.api.SendLetterResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Base64.getEncoder;
import static java.util.stream.Collectors.toList;

@Service
@Slf4j
public class BulkPrintService {

    private static final String XEROX_TYPE_PARAMETER = "DIV001";
    private static final String LETTER_TYPE_KEY = "letterType";
    private static final String CASE_REFERENCE_NUMBER_KEY = "caseReferenceNumber";
    private static final String CASE_IDENTIFIER_KEY = "caseIdentifier";
    
    private final AuthTokenGenerator authTokenGenerator;

    private final SendLetterApi sendLetterApi;
    
    private final boolean bulkPrintEnabled;

    @Autowired
    public BulkPrintService(@Value("${feature-toggle.toggle.bulk-print}") final boolean bulkPrintEnabled, 
                            final AuthTokenGenerator authTokenGenerator,
                            final SendLetterApi sendLetterApi
    ) {
        this.bulkPrintEnabled = bulkPrintEnabled;
        this.authTokenGenerator = authTokenGenerator;
        this.sendLetterApi = sendLetterApi;
    }

    /**
     * Note: the order of documents you send to this service is the order in which they will print.
     */
    public void send(final String caseId, final String letterType, final List<GeneratedDocumentInfo> documents) {
        if (bulkPrintEnabled) {
            final List<String> stringifiedDocuments = documents.stream()
                .map(GeneratedDocumentInfo::getBytes)
                .map(getEncoder()::encodeToString)
                .collect(toList());

            send(authTokenGenerator.generate(), caseId, letterType, stringifiedDocuments);

        } else {
            log.info(" Bulk print feature is toggled-off from feature toggle service");
        }
    }

    private void send(final String authToken, final String caseId, final String letterType, final List<String> documents) {
        log.info("Sending {} for case {}", letterType, caseId);
        SendLetterResponse sendLetterResponse = sendLetterApi.sendLetter(authToken,
            new LetterWithPdfsRequest(documents, XEROX_TYPE_PARAMETER, getAdditionalData(caseId, letterType)));

        log.info("Letter service produced the following letter Id {} for case {}", sendLetterResponse.letterId, caseId);
    }


    private Map<String, Object> getAdditionalData(final String caseId, final String letterType) {
        final Map<String, Object> additionalData = new HashMap<>();
        additionalData.put(LETTER_TYPE_KEY, letterType);
        additionalData.put(CASE_IDENTIFIER_KEY, caseId);
        additionalData.put(CASE_REFERENCE_NUMBER_KEY, caseId);
        return additionalData;
    }
}
