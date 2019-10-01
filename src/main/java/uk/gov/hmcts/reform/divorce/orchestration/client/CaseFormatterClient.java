package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "formatter-service-client", url = "${case.formatter.service.api.baseurl}")
public interface CaseFormatterClient {

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/caseformatter/version/1/remove/documents/{documentType}",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> removeAllDocumentsByType(
        @PathVariable("documentType") String eventId,
        @RequestBody Map<String, Object> caseData
    );
}
