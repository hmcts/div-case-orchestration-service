package uk.gov.hmcts.reform.divorce.maintenance;

import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.idam.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdSubmissionSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
public class SolicitorAmendPetitionForRefusalTest extends CcdSubmissionSupport {
    private static final String PAYLOAD_CONTEXT_PATH = "fixtures/solicitor/";

    @Value("${case.orchestration.solicitor-amend-petition-refusal.context-path}")
    private String solicitorAmendPetitionContextPath;

    @Test
    public void givenValidCase_whenSolicitorAmendPetitionForRefusalRejection_newDraftPetitionIsReturned() throws Exception {
        final UserDetails solicitorUser = createSolicitorUser();
        log.info("givenValidCase_whenSolicitorAmendPetitionForRefusalRejection_newDraftPetitionIsReturned solicitorUser: {}", solicitorUser);

        Response cosResponse = postWithData("solicitor-request-data-dn-rejection.json", solicitorUser.getAuthToken());
        log.info("givenValidCase_whenSolicitorAmendPetitionForRefusalRejection_newDraftPetitionIsReturned cosResponse: {}", cosResponse);
        assertThat(cosResponse.getStatusCode(), is(HttpStatus.OK.value()));
    }

    private Response postWithData(String pathToFileWithData, String authToken) throws Exception {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, authToken);

        return RestUtil.postToRestService(
            serverUrl + solicitorAmendPetitionContextPath,
            headers,
            ResourceLoader.loadJson(PAYLOAD_CONTEXT_PATH + pathToFileWithData));
    }

}
