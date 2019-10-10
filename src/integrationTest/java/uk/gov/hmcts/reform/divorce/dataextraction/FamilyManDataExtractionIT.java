package uk.gov.hmcts.reform.divorce.dataextraction;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.support.cos.RetrieveCaseSupport;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import static java.util.Collections.emptyMap;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.http.HttpStatus.OK;

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
        Response response = RestUtil.postToRestService(
            serverUrl + testDataExtractionEndPoint,
            emptyMap(),
            null
        );

        assertThat(response.getStatusCode(), is(OK.value()));
    }

    /**
     * Please look into class-level comment if this test is not passing locally.
     */
    @Test
    public void shouldEmailCsvFiles_ForGivenStatusAndDate() {
        Response response = RestUtil.postToRestService(
            serverUrl + testDataExtractionEndPoint + "/status/DA/lastModifiedDate/2019-10-01",
            emptyMap(),
            null
        );

        assertThat(response.getStatusCode(), is(OK.value()));
    }

}