package uk.gov.hmcts.reform.divorce.orchestration.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.Pin;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.PinRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.idam.UserDetails;

@FeignClient(name = "idam-api", url = "${idam.api.url}")
public interface IdamClient {
    @RequestMapping(method = RequestMethod.POST, value = "/pin")
    @ResponseBody Pin createPin(@RequestBody PinRequest request, @RequestHeader(HttpHeaders.AUTHORIZATION)  String authorisation);

    @RequestMapping(method = RequestMethod.GET, value = "/details")
    UserDetails retrieveUserDetails(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);
}
