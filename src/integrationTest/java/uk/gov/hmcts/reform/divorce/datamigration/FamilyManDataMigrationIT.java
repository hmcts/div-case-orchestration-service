package uk.gov.hmcts.reform.divorce.datamigration;

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
 * This is going to be used to assert that the listener side of the data migration job is called and runs successfully.
 * Ideally, we'd be testing that the e-mail was sent as expected, but as of now, we have no way of asserting that.
 */
public class FamilyManDataMigrationIT extends RetrieveCaseSupport {

    @Value("${case.orchestration.jobScheduler.migrate-data-to-family-man.context-path}")
    private String testDataMigrationEndPoint;

    @Test
    public void shouldEmailCsvFileWithCase_ForDecreeAbsoluteIssued() {
        final UserDetails caseWorkerUser = createCaseWorkerUser();

        Response response = callTestEndpoint(caseWorkerUser.getAuthToken());

        assertThat(response.getStatusCode(), is(HttpStatus.OK.value()));
    }

    private Response callTestEndpoint(String userToken) {
        final Map<String, Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        headers.put(HttpHeaders.AUTHORIZATION, userToken);

        return RestUtil.postToRestService(
            serverUrl + testDataMigrationEndPoint,
            headers,
            null,
            emptyMap()
        );
    }

}