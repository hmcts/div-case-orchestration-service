package uk.gov.hmcts.reform.divorce.dataextraction;

import io.restassured.response.Response;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.cos.RetrieveCaseSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * This is going to be used to assert that the listener side of the data extraction job is called and runs successfully.
 * Ideally, we'd be testing that the e-mail was sent as expected, but as of now, we have no way of asserting that.
 *
 * <p>To run this test locally, you'll need MailHog to be running locally. Here are some instructions to have this done using Docker:
 * #Download image
 * docker pull mailhog/mailhog
 *
 * <p>#Run new container (if it's the first time you do this)
 * docker run -d -p 32773:1025 -p 32772:8025 --name localMailHog mailhog/mailhog
 *
 * <p>#Start existing container (if it's not the first time you do this)
 * docker start localMailHog
 *
 * <p>#Log into MailHog to assert the e-mails sent by this test.
 * http://localhost:32772
 */
public class FamilyManDataExtractionIT extends RetrieveCaseSupport {

    @Value("${case.orchestration.jobScheduler.extract-data-to-family-man.context-path}")
    private String testDataExtractionEndPoint;

    /**
     * Please look into class-level comment if this test is not passing locally.
     */
    @Test
    public void shouldEmailCsvFiles() {
        final UserDetails caseWorkerUser = createCaseWorkerUser();

        Response response = callTestEndpoint(caseWorkerUser.getAuthToken());

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    private Response callTestEndpoint(String userToken) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, userToken);

        return RestUtil.postToRestService(
            serverUrl + testDataExtractionEndPoint,
            headers,
            null,
            emptyMap()
        );
    }

}