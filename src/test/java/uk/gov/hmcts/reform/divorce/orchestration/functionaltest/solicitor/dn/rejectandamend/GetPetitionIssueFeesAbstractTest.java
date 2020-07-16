package uk.gov.hmcts.reform.divorce.orchestration.functionaltest.solicitor.dn.rejectandamend;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.functionaltest.MockedFunctionalTest;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_AMOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_CODE;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FEE_VERSION;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;

public abstract class GetPetitionIssueFeesAbstractTest extends MockedFunctionalTest {

    static final String API_URL = "/petition-issue-fees";
    private static final String PETITION_ISSUE_FEE_CONTEXT_PATH = "/fees-and-payments/version/1/petition-issue-fee";
    private static final String PETITION_AMENDMENT_ISSUE_FEE_CONTEXT_PATH = "/fees-and-payments/version/1/amend-fee";
    private static final String ADD_PETITIONER_SOLICITOR_ROLE = String
        .format("/casemaintenance/version/1/add-petitioner-solicitor-role/%s", TEST_CASE_ID);

    CcdCallbackRequest callbackRequest;

    FeeResponse issueFeeResponse;

    FeeResponse amendFeeResponse;

    @Autowired
    MockMvc webClient;

    @Before
    public final void setUp() {
        Map<String, Object> caseData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .caseData(caseData)
            .caseId(TEST_CASE_ID)
            .state(TEST_STATE)
            .build();
        callbackRequest = CcdCallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        issueFeeResponse = FeeResponse.builder()
            .amount(TEST_FEE_AMOUNT)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();
        stubGetFeeFromFeesAndPayments(issueFeeResponse, false);

        amendFeeResponse = FeeResponse.builder()
            .amount(95d)
            .feeCode(TEST_FEE_CODE)
            .version(TEST_FEE_VERSION)
            .description(TEST_FEE_DESCRIPTION)
            .build();
        stubGetFeeFromFeesAndPayments(amendFeeResponse, true);
    }

    void stubGetFeeFromFeesAndPayments(FeeResponse feeResponse, boolean petitionAmendment) {
        String url = petitionAmendment ? PETITION_AMENDMENT_ISSUE_FEE_CONTEXT_PATH : PETITION_ISSUE_FEE_CONTEXT_PATH;
        feesAndPaymentsServer.stubFor(WireMock.get(url)
            .willReturn(aResponse()
                .withStatus(HttpStatus.OK.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)
                .withBody(convertObjectToJsonString(feeResponse))));
    }

    void stubMaintenanceServerEndpointForAddPetitionerSolicitorRole(HttpStatus status) {
        maintenanceServiceServer.stubFor(put(ADD_PETITIONER_SOLICITOR_ROLE)
            .withHeader(AUTHORIZATION, new EqualToPattern(AUTH_TOKEN))
            .willReturn(aResponse()
                .withStatus(status.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_UTF8_VALUE)));
    }

}