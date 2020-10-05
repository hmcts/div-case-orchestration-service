package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.orchestration.client.PbaValidationClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.validation.OrganisationEntityResponse;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.pay.validation.PBAOrganisationResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.BEARER_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESP_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SERVICE_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PBA_NUMBERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_PAY_BY_ACCOUNT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SOLICITOR_HOW_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DynamicList.asDynamicList;

@RunWith(MockitoJUnitRunner.class)
public class GetPbaNumbersTaskTest {

    @Mock
    private PbaValidationClient pbaValidationClient;

    @Mock
    private AuthTokenGenerator serviceAuthGenerator;

    @Mock
    private ResponseEntity responseEntity;

    @Mock
    private IdamClient idamClient;

    @Mock
    private AuthUtil authUtil;

    @InjectMocks
    private GetPbaNumbersTask getPbaNumbersTask;

    private TaskContext context;
    private Map<String, Object> caseData;
    private PBAOrganisationResponse successfulResponse;
    private PBAOrganisationResponse emptyResponse;
    private List<String> expectedPbaNumbers = ImmutableList.of("pbaNumber1", "pbaNumber2");

    @Before
    public void setup() {
        context = TaskContextHelper.contextWithToken();

        caseData = new HashMap<>();
        caseData.put(SOLICITOR_HOW_TO_PAY_JSON_KEY, FEE_PAY_BY_ACCOUNT);

        successfulResponse = buildPbaResponse(expectedPbaNumbers);
        emptyResponse = buildPbaResponse(Collections.emptyList());


        when(serviceAuthGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        when(authUtil.getBearToken(AUTH_TOKEN)).thenReturn(BEARER_AUTH_TOKEN);
        when(idamClient.getUserDetails(BEARER_AUTH_TOKEN))
            .thenReturn(UserDetails.builder().email(TEST_RESP_SOLICITOR_EMAIL).build());
    }

    @Test
    public void givenNotPayByAccount_whenExecuteIsCalled_thenReturnData() {
        caseData.replace(SOLICITOR_HOW_TO_PAY_JSON_KEY, "NotByAccount");

        assertEquals(caseData, getPbaNumbersTask.execute(context, caseData));

        verifyNoInteractions(authUtil);
        verifyNoInteractions(idamClient);
        verifyNoInteractions(serviceAuthGenerator);
        verifyNoInteractions(pbaValidationClient);
    }

    @Test(expected = TaskException.class)
    public void givenMissingData_whenExecuteIsCalled_thenThrowTaskException() {
        assertEquals(caseData, getPbaNumbersTask.execute(new DefaultTaskContext(), caseData));

        verifyNoInteractions(authUtil);
        verifyNoInteractions(idamClient);
        verifyNoInteractions(serviceAuthGenerator);
        verifyNoInteractions(pbaValidationClient);
    }

    @Test
    public void givenNoPbaNumbersFound_whenExecuteIsCalled_thenReturnDataWithNoPbaNumbers() {
        Map<String, Object> expectedCaseData = new HashMap<>(caseData);

        caseData.put(PBA_NUMBERS, ImmutableList.of("oldPbaNumber1", "oldPbaNumber2"));

        whenRetrievePbaNumbersReturn(emptyResponse);

        assertEquals(expectedCaseData, getPbaNumbersTask.execute(context, caseData));

        verify(authUtil).getBearToken(AUTH_TOKEN);
        verify(idamClient).getUserDetails(BEARER_AUTH_TOKEN);
        verify(serviceAuthGenerator).generate();
        verifyRetrievePbaNumbersCalledOnce();
    }

    @Test
    public void givenNull_whenExecuteIsCalled_thenReturnDataWithNoPbaNumbers() {
        Map<String, Object> expectedCaseData = new HashMap<>(caseData);

        caseData.put(PBA_NUMBERS, ImmutableList.of("oldPbaNumber1", "oldPbaNumber2"));

        whenRetrievePbaNumbersReturn(buildPbaResponse(null));

        assertEquals(expectedCaseData, getPbaNumbersTask.execute(context, caseData));

        verify(authUtil).getBearToken(AUTH_TOKEN);
        verify(idamClient).getUserDetails(BEARER_AUTH_TOKEN);
        verify(serviceAuthGenerator).generate();
        verifyRetrievePbaNumbersCalledOnce();
    }

    @Test
    public void givenSomePbaNumbersFound_whenExecuteIsCalled_thenReturnDataWithPbaNumbers() {
        Map<String, Object> expectedCaseData = new HashMap<>(caseData);
        expectedCaseData.put(PBA_NUMBERS, asDynamicList(expectedPbaNumbers));

        whenRetrievePbaNumbersReturn(successfulResponse);

        assertEquals(expectedCaseData, getPbaNumbersTask.execute(context, caseData));

        verify(authUtil).getBearToken(AUTH_TOKEN);
        verify(idamClient).getUserDetails(BEARER_AUTH_TOKEN);
        verify(serviceAuthGenerator).generate();
        verifyRetrievePbaNumbersCalledOnce();
    }

    private PBAOrganisationResponse buildPbaResponse(List<String> pbaNumbers) {
        return PBAOrganisationResponse.builder()
            .organisationEntityResponse(
                OrganisationEntityResponse.builder()
                    .paymentAccount(pbaNumbers)
                    .build())
            .build();
    }

    private void whenRetrievePbaNumbersReturn(PBAOrganisationResponse response) {
        when(responseEntity.getBody()).thenReturn(response);
        when(pbaValidationClient.retrievePbaNumbers(
            AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            TEST_RESP_SOLICITOR_EMAIL))
            .thenReturn(responseEntity);
    }

    private void verifyRetrievePbaNumbersCalledOnce() {
        verify(pbaValidationClient).retrievePbaNumbers(
            AUTH_TOKEN,
            TEST_SERVICE_AUTH_TOKEN,
            TEST_RESP_SOLICITOR_EMAIL);
    }
}
