package uk.gov.hmcts.reform.divorce.orchestration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentUpdateRequest;

import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "formatter-service-client", url = "${case.formatter.service.api.baseurl}")
public interface CaseFormatterClient {

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/caseformatter/version/1/add-documents",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> addDocuments(
        @RequestBody DocumentUpdateRequest documentUpdateRequest);

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/caseformatter/version/1/to-ccd-format",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> transformToCCDFormat(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
        @RequestBody Map<String, Object> transformToCCDFormat
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/caseformatter/version/1/to-divorce-format",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> transformToDivorceFormat(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationToken,
        @RequestBody Map<String, Object> transformToDivorceFormat
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/caseformatter/version/1/to-aos-submit-format",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> transformToAosCaseFormat(
        @RequestBody Map<String, Object> divorceSession
    );

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/caseformatter/version/1/to-dn-submit-format",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> transformToDnCaseFormat(
            @RequestBody Map<String, Object> divorceSession
    );

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/caseformatter/version/1/remove-all-petition-documents",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> removeAllPetitionDocuments(
            @RequestBody Map<String, Object> caseData
    );

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/caseformatter/version/1/to-da-submit-format",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> transformToDaCaseFormat(
            @RequestBody Map<String, Object> divorceSession
    );
}
