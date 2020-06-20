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
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.CO_RESPONDENT_SOLICITOR_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.util.DerivedAddressFormatterHelper.D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS;
import static uk.gov.hmcts.reform.divorce.util.ResourceLoader.objectToJson;

public class ProcessAosOfflineAnswersTest extends IntegrationTest {

    private static final String CCD_CALLBACK_REQUEST = "fixtures/co-resp-case/co-resp-aos-answers.json";
    private static final String ADDRESS_LINE_1 = "AddressLine1";
    private static final String ADDRESS_LINE_2 = "AddressLine2";
    private static final String ADDRESS_LINE_3 = "AddressLine3";
    private static final String COUNTY = "County";
    private static final String COUNTRY = "Country";
    private static final String POST_TOWN = "PostTown";
    private static final String POST_CODE = "PostCode";
    private static final String expectedAddress = "ADDY_LINE_1\nADDY_LINE_2\nADDY_LINE_3\nADDY_COUNTY\nADDY_COUNTRY\nADDY_POST_TOWN\nADDY_POSTCODE";

    @Autowired
    private CosApiClient cosApiClient;

    private CcdCallbackRequest ccdCallbackRequest;

    @Before
    public void setUp() {
        ccdCallbackRequest = ResourceLoader.loadJsonToObject(CCD_CALLBACK_REQUEST, CcdCallbackRequest.class);
    }

    @Test
    public void givenCase_whenProcessAosOfflineAnswersEvent_shouldReturnCallbackResponse_With_ReceivedAosFromCoResp_Yes_Value() {

        CcdCallbackResponse response = cosApiClient.processAosPackOfflineAnswers(ccdCallbackRequest, CO_RESPONDENT.getDescription());

        String jsonResponse = objectToJson(response);
        assertThat(jsonResponse, hasJsonPath("$.data.ReceivedAosFromCoResp", is(YES_VALUE)));
    }

    @Test
    public void givenCase_whenProcessAosOfflineAnswersEventAndCoRespondentIsRepresented_shouldFormatDerivedSolicitorAddress() {
        addRepresentedCoRespondentDataToCcdCallbackRequest(ccdCallbackRequest);

        CcdCallbackResponse ccdCallbackResponse = cosApiClient.processAosPackOfflineAnswers(ccdCallbackRequest, CO_RESPONDENT.getDescription());

        assertThat(ccdCallbackResponse.getErrors(), is(nullValue()));
        assertThat(ccdCallbackResponse.getWarnings(), is(nullValue()));
        String jsonResponse = objectToJson(ccdCallbackResponse);
        assertThat(jsonResponse, hasJsonPath("$.data.CoRespondentSolicitorRepresented", is(YES_VALUE)));
        assertThat(jsonResponse, hasJsonPath("$.data.DerivedCoRespondentSolicitorAddr", is(expectedAddress)));
        assertThat(jsonResponse, hasNoJsonPath("$.data.D8DerivedReasonForDivorceAdultery3rdAddr"));
    }

    @Test
    public void givenCase_whenProcessAosOfflineAnswersEventAndCoRespondentIsNotRepresented_shouldFormatDerivedCoRespondentAddress() {
        addNonRepresentedCoRespondentDataToCcdCallbackRequest(ccdCallbackRequest);

        CcdCallbackResponse ccdCallbackResponse = cosApiClient.processAosPackOfflineAnswers(ccdCallbackRequest, CO_RESPONDENT.getDescription());

        assertThat(ccdCallbackResponse.getErrors(), is(nullValue()));
        assertThat(ccdCallbackResponse.getWarnings(), is(nullValue()));
        String jsonResponse = objectToJson(ccdCallbackResponse);
        assertThat(jsonResponse, hasJsonPath("$.data.CoRespondentSolicitorRepresented", is(NO_VALUE)));
        assertThat(jsonResponse, hasJsonPath("$.data.D8DerivedReasonForDivorceAdultery3rdAddr", is(expectedAddress)));
        assertThat(jsonResponse, hasNoJsonPath("$.data.DerivedCoRespondentSolicitorAddr"));
    }

    private void addRepresentedCoRespondentDataToCcdCallbackRequest(CcdCallbackRequest ccdCallbackRequest) {
        ImmutableMap<String, Object> address = getBuildAddress();
        ccdCallbackRequest.getCaseDetails().getCaseData().put(CO_RESPONDENT_SOLICITOR_ADDRESS, address);
        ccdCallbackRequest.getCaseDetails().getCaseData().put(CO_RESPONDENT_REPRESENTED, YES_VALUE);
    }

    private void addNonRepresentedCoRespondentDataToCcdCallbackRequest(CcdCallbackRequest ccdCallbackRequest) {
        ImmutableMap<String, Object> address = getBuildAddress();
        ccdCallbackRequest.getCaseDetails().getCaseData().put(D8_REASON_FOR_DIVORCE_ADULTERY_3RD_PARTY_ADDRESS, address);
        ccdCallbackRequest.getCaseDetails().getCaseData().put(CO_RESPONDENT_REPRESENTED, NO_VALUE);
    }

    private ImmutableMap<String, Object> getBuildAddress() {
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
