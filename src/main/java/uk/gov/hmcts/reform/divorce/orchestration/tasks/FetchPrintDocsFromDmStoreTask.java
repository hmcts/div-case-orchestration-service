package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.rest.RestRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;

@Component
@Slf4j
@RequiredArgsConstructor
public class FetchPrintDocsFromDmStoreTask implements Task<Map<String, Object>> {

    private static final String CASEWORKER_DIVORCE = "caseworker-divorce";
    private static final String USER_ROLES = "user-roles";

    private final AuthTokenGenerator authTokenGenerator;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) {

        Map<String, GeneratedDocumentInfo> generatedDocumentInfoList = extractGeneratedDocumentList(caseData);
        populateDocumentBytes(context, generatedDocumentInfoList);
        context.setTransientObject(DOCUMENTS_GENERATED, generatedDocumentInfoList);

        return caseData;
    }

    private Map<String, GeneratedDocumentInfo> extractGeneratedDocumentList(Map<String, Object> caseData) {
        List<CollectionMember<Document>> generatedDocumentList = ofNullable(caseData.get(D8DOCUMENTS_GENERATED))
            .map(i -> objectMapper.convertValue(i, new TypeReference<List<CollectionMember<Document>>>() {
            }))
            .orElse(new ArrayList<>());
        Map<String, GeneratedDocumentInfo> generatedDocumentInfoList = new HashMap<>();
        for (CollectionMember<Document> document : generatedDocumentList) {
            Document value = document.getValue();
            String documentType = value.getDocumentType();
            DocumentLink documentLink = value.getDocumentLink();

            if (documentLink != null) {
                GeneratedDocumentInfo gdi = GeneratedDocumentInfo.builder()
                    .documentType(value.getDocumentType())
                    .url(documentLink.getDocumentBinaryUrl())
                    .documentType(documentType)
                    .fileName(documentLink.getDocumentFilename())
                    .build();
                generatedDocumentInfoList.put(documentType, gdi);
            }
        }

        return generatedDocumentInfoList;
    }

    private void populateDocumentBytes(TaskContext context, Map<String, GeneratedDocumentInfo> generatedDocumentsInfo) {
        CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);
        for (GeneratedDocumentInfo generatedDocumentInfo : generatedDocumentsInfo.values()) {
            HttpHeaders headers = new HttpHeaders();
            headers.set(SERVICE_AUTHORIZATION_HEADER, authTokenGenerator.generate());
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

}