package uk.gov.hmcts.reform.divorce.support.cos;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.divorce.context.ServiceContextConfiguration;

import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "case-orchestration-api", url = "${case.orchestration.service.base.uri}",
        configuration = ServiceContextConfiguration.class)
public interface CosApiClient {

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/aos-received",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> aosReceived(@RequestHeader(AUTHORIZATION) String authorisation,
                                   @RequestBody Map<String, Object> caseDataContent
    );

    @RequestMapping(
            method = RequestMethod.GET,
            value = "/draftsapi/version/1",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> getDraft(@RequestHeader(AUTHORIZATION) String authorisation);

    @RequestMapping(
            method = RequestMethod.PUT,
            value = "/draftsapi/version/1",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> saveDraft(@RequestHeader(AUTHORIZATION) String authorisation,
                                  @RequestBody JsonNode caseDataContent,
                   @RequestParam(name = "notificationEmail") String notificationEmail
   );

    @RequestMapping(
            method = RequestMethod.DELETE,
            value = "/draftsapi/version/1",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> deleteDraft(@RequestHeader(AUTHORIZATION) String authorisation);

    @RequestMapping(
            method = RequestMethod.POST,
            value = "/submit",
            headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> submitCase(@RequestHeader(AUTHORIZATION) String authorisation,
                   @RequestBody JsonNode caseDataContent
    );
}
