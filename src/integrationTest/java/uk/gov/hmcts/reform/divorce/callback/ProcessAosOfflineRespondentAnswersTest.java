package uk.gov.hmcts.reform.divorce.callback;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.context.IntegrationTest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;
import uk.gov.hmcts.reform.divorce.support.cos.CosApiClient;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.UNREASONABLE_BEHAVIOUR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.CaseDataKeys.D8_RESPONDENT_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.CaseDataKeys.D8_RESPONDENT_HOME_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.CaseDataKeys.D8_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class ProcessAosOfflineRespondentAnswersTest extends IntegrationTest {

    private static final String CCD_CALLBACK_REQUEST = "fixtures/co-resp-case/co-resp-aos-answers.json";
    private static final String ADDRESS_LINE_1 = "AddressLine1";
    private static final String ADDRESS_LINE_2 = "AddressLine2";
    private static final String ADDRESS_LINE_3 = "AddressLine3";
    private static final String COUNTY = "County";
    private static final String COUNTRY = "Country";
    private static final String POST_TOWN = "PostTown";
    private static final String POST_CODE = "PostCode";
    private static final String EXPECTED_STRINGIFIED_ADDRESS = "ADDY_LINE_1\nADDY_LINE_2\nADDY_LINE_3\nADDY_COUNTY\n"
        + "ADDY_COUNTRY\nADDY_POST_TOWN\nADDY_POSTCODE";

    @Autowired
    private CosApiClient cosApiClient;

    private CcdCallbackRequest ccdCallbackRequest;

    @Before
    public void setUp() {
        ccdCallbackRequest = ResourceLoader.loadJsonToObject(CCD_CALLBACK_REQUEST, CcdCallbackRequest.class);
    }

    @Test
    public void givenCase_whenProcessAosOfflineAnswersEventAndRespondentIsRepresented_shouldFormatDerivedSolicitorAddress() {
        addRepresentedRespondentDataToCcdCallbackRequest(ccdCallbackRequest);

        CcdCallbackResponse ccdCallbackResponse = cosApiClient.processAosPackOfflineAnswers(ccdCallbackRequest, RESPONDENT.getDescription());

        assertThat(ccdCallbackResponse.getErrors(), is(nullValue()));
        assertThat(ccdCallbackResponse.getWarnings(), is(nullValue()));

        assertThat(objectToJson(ccdCallbackResponse), isJson(allOf(
            withJsonPath("$.data.respondentSolicitorRepresented", is(YES_VALUE)),
            withJsonPath("$.data.D8DerivedRespondentSolicitorAddr", is(EXPECTED_STRINGIFIED_ADDRESS)),
            withJsonPath("$.data.D8RespondentSolicitorAddress"),
            withoutJsonPath("$.data.D8DerivedRespondentHomeAddress")
        )));
    }

    @Test
    public void givenCase_whenProcessAosOfflineAnswersEventAndRespondentIsNotRepresented_shouldFormatDerivedRespondentHomeAddress() {
        addNonRepresentedRespondentDataToCcdCallbackRequest(ccdCallbackRequest);

        CcdCallbackResponse ccdCallbackResponse = cosApiClient.processAosPackOfflineAnswers(ccdCallbackRequest, RESPONDENT.getDescription());

        assertThat(ccdCallbackResponse.getErrors(), is(nullValue()));
        assertThat(ccdCallbackResponse.getWarnings(), is(nullValue()));

        assertThat(objectToJson(ccdCallbackResponse), isJson(allOf(
            withJsonPath("$.data.respondentSolicitorRepresented", is(NO_VALUE)),
            withJsonPath("$.data.D8DerivedRespondentHomeAddress", is(EXPECTED_STRINGIFIED_ADDRESS)),
            withoutJsonPath("$.data.D8DerivedRespondentSolicitorAddr")
        )));
    }

    @Test
    public void givenCase_whenProcessAosOfflineAnswersEventAndNoRespondentCorrespondenceAddress_shouldFormatRespondentHomeAddress() {
        createDataWithNoRespondentCorrespondenceCcdCallbackRequest(ccdCallbackRequest);

        CcdCallbackResponse ccdCallbackResponse = cosApiClient.processAosPackOfflineAnswers(ccdCallbackRequest, RESPONDENT.getDescription());

        assertThat(ccdCallbackResponse.getErrors(), is(nullValue()));
        assertThat(ccdCallbackResponse.getWarnings(), is(nullValue()));

        assertThat(objectToJson(ccdCallbackResponse), isJson(allOf(
            withJsonPath("$.data.respondentSolicitorRepresented", is(NO_VALUE)),
            withJsonPath("$.data.D8DerivedRespondentCorrespondenceAddr", is(EXPECTED_STRINGIFIED_ADDRESS)),
            withoutJsonPath("$.data.D8RespondentCorrespondenceAddress")
        )));
    }

    @Test
    public void givenCase_whenNoRespondentCorrespondenceAddress_shouldCorrectlySetUseRespontenHomeAddress() {
        createDataWithNoRespondentCorrespondenceCcdCallbackRequest(ccdCallbackRequest);

        CcdCallbackResponse ccdCallbackResponse = cosApiClient.processAosPackOfflineAnswers(ccdCallbackRequest, RESPONDENT.getDescription());

        assertThat(ccdCallbackResponse.getErrors(), is(nullValue()));
        assertThat(ccdCallbackResponse.getWarnings(), is(nullValue()));

        assertThat(objectToJson(ccdCallbackResponse), hasJsonPath("$.data.D8RespondentCorrespondenceUseHomeAddress", is(YES_VALUE)));
    }

    @Test
    public void givenCase_whenRespondentCorrespondenceAddress_shouldCorrectlySetUseRespontenHomeAddress() {
        createDataWithRespondentCorrespondenceCcdCallbackRequest(ccdCallbackRequest);

        CcdCallbackResponse ccdCallbackResponse = cosApiClient.processAosPackOfflineAnswers(ccdCallbackRequest, RESPONDENT.getDescription());

        assertThat(ccdCallbackResponse.getErrors(), is(nullValue()));
        assertThat(ccdCallbackResponse.getWarnings(), is(nullValue()));

        assertThat(objectToJson(ccdCallbackResponse), hasJsonPath("$.data.D8RespondentCorrespondenceUseHomeAddress", is(NO_VALUE)));
    }

    private void addRepresentedRespondentDataToCcdCallbackRequest(CcdCallbackRequest ccdCallbackRequest) {
        addToCaseData(ccdCallbackRequest, D8_RESPONDENT_SOLICITOR_ADDRESS, buildAddress());
        addToCaseData(ccdCallbackRequest, RESP_SOL_REPRESENTED, YES_VALUE);
        addToCaseData(ccdCallbackRequest, D_8_REASON_FOR_DIVORCE, UNREASONABLE_BEHAVIOUR);
    }

    private void addNonRepresentedRespondentDataToCcdCallbackRequest(CcdCallbackRequest ccdCallbackRequest) {
        addToCaseData(ccdCallbackRequest, D8_RESPONDENT_HOME_ADDRESS, buildAddress());
        addToCaseData(ccdCallbackRequest, RESP_SOL_REPRESENTED, NO_VALUE);
        addToCaseData(ccdCallbackRequest, D_8_REASON_FOR_DIVORCE, UNREASONABLE_BEHAVIOUR);
    }

    private void createDataWithNoRespondentCorrespondenceCcdCallbackRequest(CcdCallbackRequest ccdCallbackRequest) {
        addToCaseData(ccdCallbackRequest, D8_RESPONDENT_HOME_ADDRESS, buildAddress());
        addToCaseData(ccdCallbackRequest, RESP_SOL_REPRESENTED, NO_VALUE);
        addToCaseData(ccdCallbackRequest, D_8_REASON_FOR_DIVORCE, UNREASONABLE_BEHAVIOUR);
    }

    private void createDataWithRespondentCorrespondenceCcdCallbackRequest(CcdCallbackRequest ccdCallbackRequest) {
        addToCaseData(ccdCallbackRequest, D8_RESPONDENT_CORRESPONDENCE_ADDRESS, buildAddress());
        addToCaseData(ccdCallbackRequest, RESP_SOL_REPRESENTED, NO_VALUE);
        addToCaseData(ccdCallbackRequest, D_8_REASON_FOR_DIVORCE, UNREASONABLE_BEHAVIOUR);
    }

    private void addToCaseData(CcdCallbackRequest ccdCallbackRequest, String key, Object value) {
        ccdCallbackRequest.getCaseDetails().getCaseData().put(key, value);
    }

    private ImmutableMap<String, Object> buildAddress() {
        return ImmutableMap.<String, Object>builder()
            .put(ADDRESS_LINE_1, "ADDY_LINE_1")
            .put(ADDRESS_LINE_2, "ADDY_LINE_2")
            .put(ADDRESS_LINE_3, "ADDY_LINE_3")
            .put(COUNTY, "ADDY_COUNTY")
            .put(COUNTRY, "ADDY_COUNTRY")
            .put(POST_TOWN, "ADDY_POST_TOWN")
            .put(POST_CODE, "ADDY_POSTCODE")
            .build();
    }

}
