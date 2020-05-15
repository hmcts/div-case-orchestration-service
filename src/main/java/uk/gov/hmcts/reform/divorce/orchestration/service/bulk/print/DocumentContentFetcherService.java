package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.rest.RestRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;

@Slf4j
@Component
@AllArgsConstructor
public class DocumentContentFetcherService {

    public static final String CALL_DM_ON_BEHALF = "caseworker-divorce";

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Headers {
        public static final String SERVICE_AUTHORIZATION = OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;
        public static final String USER_ROLES = "user-roles";
    }

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator authTokenGenerator;

    public GeneratedDocumentInfo fetchPrintContent(GeneratedDocumentInfo document) {
        ResponseEntity<byte[]> response = callDocStore(document);

        return GeneratedDocumentInfo.builder()
            .documentType(document.getDocumentType())
            .url(document.getUrl())
            .mimeType(document.getMimeType())
            .fileName(document.getFileName())
            .createdOn(document.getCreatedOn())
            .bytes(response.getBody())
            .build();
    }

    private ResponseEntity<byte[]> callDocStore(GeneratedDocumentInfo document) {
        HttpEntity<RestRequest> httpEntity = getRequestHeaderForCaseWorker();
        ResponseEntity<byte[]> response = restTemplate.exchange(document.getUrl(), HttpMethod.GET, httpEntity, byte[].class);

        log.info("Try to fetch content of document from DM {}, {}", document.getFileName(), document.getUrl());

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("Failed to get bytes from document store for document {}, {}", document.getFileName(), document.getUrl());
            throw new RuntimeException(String.format("Unexpected code from DM store: %s ", response.getStatusCode()));
        }

        log.info("Fetch content of document from DM {}, size: {}", document.getFileName(), response.getBody().length);

        return response;
    }

    private HttpEntity<RestRequest> getRequestHeaderForCaseWorker() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(Headers.SERVICE_AUTHORIZATION, authTokenGenerator.generate());
        headers.set(Headers.USER_ROLES, CALL_DM_ON_BEHALF);

        return new HttpEntity<>(headers);
    }
}
