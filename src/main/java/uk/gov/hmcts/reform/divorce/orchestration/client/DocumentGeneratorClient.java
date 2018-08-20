package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "document-generator-client", url = "${document.generator.service.api.baseurl}")
public interface DocumentGeneratorClient {

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/version/1/generatePDF",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    GeneratedDocumentInfo generatePDF(
        @RequestBody GenerateDocumentRequest generateDocumentRequest,
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken);
}
