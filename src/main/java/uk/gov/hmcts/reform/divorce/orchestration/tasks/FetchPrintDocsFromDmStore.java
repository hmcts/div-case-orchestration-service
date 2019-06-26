package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.rest.RestRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;

@Component
@Slf4j
public class FetchPrintDocsFromDmStore implements Task<Map<String, Object>> {

    private static final String DOCUMENT_LINK = "DocumentLink";

    private static final String VALUE = "value";

    private static final String DOCUMENT_URL = "document_binary_url";

    private static final String DOCUMENT_TYPE = "DocumentType";

    private static final String DOCUMENT_FILENAME = "document_filename";

    private static final String DOCUMENTS_GENERATED = "DocumentsGenerated";

    private static final String CASEWORKER_DIVORCE = "caseworker-divorce";

    private static final String USER_ROLES = "user-roles";

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private AuthTokenGenerator authTokenGenerator;
    private final RestTemplate restTemplate;

    public FetchPrintDocsFromDmStore(AuthTokenGenerator authTokenGenerator, RestTemplate restTemplate) {
        this.authTokenGenerator = authTokenGenerator;
        this.restTemplate = restTemplate;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        Map<String, GeneratedDocumentInfo> generatedDocumentInfoList = extractGeneratedDocumentList(caseData);
        populateDocumentBytes(context, generatedDocumentInfoList);
        context.setTransientObject(DOCUMENTS_GENERATED, generatedDocumentInfoList);
        return caseData;
    }

    private void populateDocumentBytes(TaskContext context, Map<String, GeneratedDocumentInfo> generatedDocumentInfos) {
        CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);
        for (GeneratedDocumentInfo generatedDocumentInfo : generatedDocumentInfos.values()) {
            HttpHeaders headers = new HttpHeaders();
            headers.set(SERVICE_AUTHORIZATION, authTokenGenerator.generate());
            headers.set(USER_ROLES, CASEWORKER_DIVORCE);
            HttpEntity<RestRequest> httpEntity = new HttpEntity<>(headers);

            ResponseEntity<byte[]> response = restTemplate.exchange(generatedDocumentInfo.getUrl(), HttpMethod.GET, httpEntity, byte[].class);
            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("Failed to get bytes from document store for document {} in case Id {}",
                        generatedDocumentInfo.getUrl(), caseDetails.getCaseId());
                throw new RuntimeException(String.format("Unexpected code from DM store: %s ", response.getStatusCode()));
            }
            generatedDocumentInfo.setBytes(response.getBody());
        }
    }

    /**
     * I'm not using object mapper here to keep it consistent with rest of code, when we migrate the formatter
     * service to as module dependency this method could be simplified.
     */
    @SuppressWarnings("unchecked")
    private Map<String, GeneratedDocumentInfo> extractGeneratedDocumentList(Map<String, Object> caseData) {
        List<Map> documentList =
            ofNullable(caseData.get(D8DOCUMENTS_GENERATED)).map(i -> (List<Map>) i).orElse(new ArrayList<>());
        Map<String, GeneratedDocumentInfo> generatedDocumentInfoList = new HashMap<>();
        for (Map<String, Object> document : documentList) {
            Map<String, Object> value = ((Map) document.get(VALUE));
            String documentType = getStringValue(value, DOCUMENT_TYPE);
            Map<String, Object> documentLink = (Map) ofNullable(getValue(value, DOCUMENT_LINK)).orElse(null);

            if (ofNullable(documentLink).isPresent()) {
                GeneratedDocumentInfo gdi = GeneratedDocumentInfo.builder()
                    .documentType(getStringValue(value, DOCUMENT_TYPE))
                    .url(getStringValue(documentLink, DOCUMENT_URL))
                    .documentType(documentType)
                    .fileName(getStringValue(documentLink, DOCUMENT_FILENAME))
                    .build();
                generatedDocumentInfoList.put(documentType, gdi);
            }
        }

        return generatedDocumentInfoList;
    }

    private Object getValue(Map<String, Object> objectMap, String key) {
        Iterator<Map.Entry<String, Object>> iterator = objectMap.entrySet().iterator();
        Object result = null;
        while (iterator.hasNext()) {
            Map.Entry map = iterator.next();
            if (map.getKey().equals(key)) {
                result = map.getValue();
            }
        }
        return result;
    }

    private String getStringValue(Map<String, Object> objectMap, String key) {
        return ofNullable(getValue(objectMap, key)).map(Object::toString).orElse(StringUtils.EMPTY);
    }

}
