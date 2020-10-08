package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.UserDivorcePartyLookup;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.PETITIONER;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.convertObjectToJsonString;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getObjectMapperInstance;

@RunWith(MockitoJUnitRunner.class)
public class GeneralOrdersFilterTaskTest {

    private static final String GENERAL_ORDER_AVAILABLE_TO_ALL_PARTIES = "9320ad41-4980-47b7-885d-76daae69ff9b";
    private static final String GENERAL_ORDER_AVAILABLE_TO_PETITIONER_ONLY = "649111b0-1aad-446d-b979-27f5862af583";
    private static final String GENERAL_ORDER_AVAILABLE_TO_RESPONDENT_ONLY = "821e57ae-a9f8-426d-a11c-7c099e25ad31";
    private static final String GENERAL_ORDER_AVAILABLE_TO_CO_RESPONDENT_ONLY = "e45691eb-292b-4684-bb51-4f4814b1cfef";

    @Mock
    private UserDivorcePartyLookup userDivorcePartyLookup;

    private GeneralOrdersFilterTask generalOrdersFilterTask;

    private Map<String, Object> incomingCaseData;
    private TaskContext taskContext;

    @Before
    public void setUp() throws IOException {
        generalOrdersFilterTask = new GeneralOrdersFilterTask(userDivorcePartyLookup, new CcdUtil(null, getObjectMapperInstance(), null));

        incomingCaseData = getJsonFromResourceFile("/jsonExamples/payloads/general/order/case-data.json", new TypeReference<>() {
        });

        taskContext = new DefaultTaskContext();
        taskContext.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);
    }

    @Test
    public void shouldOnlyReturnGeneralOrdersAuthorisedToPetitioner() {
        when(userDivorcePartyLookup.lookupDivorcePartForGivenUser(AUTH_TOKEN, incomingCaseData)).thenReturn(Optional.of(PETITIONER));

        Map<String, Object> returnedCaseData = generalOrdersFilterTask.execute(taskContext, incomingCaseData);

        String returnedCaseDataJson = convertObjectToJsonString(returnedCaseData);
        assertThat(returnedCaseDataJson, hasJsonPath("$.GeneralOrders", hasSize(2)));
        assertThat(returnedCaseDataJson, hasJsonPath("$.GeneralOrders", hasItems(
            hasJsonPath("$.id", is(GENERAL_ORDER_AVAILABLE_TO_ALL_PARTIES)),
            hasJsonPath("$.id", is(GENERAL_ORDER_AVAILABLE_TO_PETITIONER_ONLY))
        )));
    }

    @Test
    public void shouldOnlyReturnGeneralOrdersAuthorisedToRespondent() {
        when(userDivorcePartyLookup.lookupDivorcePartForGivenUser(AUTH_TOKEN, incomingCaseData)).thenReturn(Optional.of(RESPONDENT));

        Map<String, Object> returnedCaseData = generalOrdersFilterTask.execute(taskContext, incomingCaseData);

        String returnedCaseDataJson = convertObjectToJsonString(returnedCaseData);
        assertThat(returnedCaseDataJson, hasJsonPath("$.GeneralOrders", hasSize(2)));
        assertThat(returnedCaseDataJson, hasJsonPath("$.GeneralOrders", hasItems(
            hasJsonPath("$.id", is(GENERAL_ORDER_AVAILABLE_TO_ALL_PARTIES)),
            hasJsonPath("$.id", is(GENERAL_ORDER_AVAILABLE_TO_RESPONDENT_ONLY))
        )));
    }

    @Test
    public void shouldOnlyReturnGeneralOrdersAuthorisedToCoRespondent() {
        when(userDivorcePartyLookup.lookupDivorcePartForGivenUser(AUTH_TOKEN, incomingCaseData)).thenReturn(Optional.of(CO_RESPONDENT));

        Map<String, Object> returnedCaseData = generalOrdersFilterTask.execute(taskContext, incomingCaseData);

        String returnedCaseDataJson = convertObjectToJsonString(returnedCaseData);
        assertThat(returnedCaseDataJson, hasJsonPath("$.GeneralOrders", hasSize(2)));
        assertThat(returnedCaseDataJson, hasJsonPath("$.GeneralOrders", hasItems(
            hasJsonPath("$.id", is(GENERAL_ORDER_AVAILABLE_TO_ALL_PARTIES)),
            hasJsonPath("$.id", is(GENERAL_ORDER_AVAILABLE_TO_CO_RESPONDENT_ONLY))
        )));
    }

    @Test
    public void shouldReturnNoGeneralOrdersForUserWhoIsNotDivorceParty() {
        when(userDivorcePartyLookup.lookupDivorcePartForGivenUser(AUTH_TOKEN, incomingCaseData)).thenReturn(Optional.empty());

        Map<String, Object> returnedCaseData = generalOrdersFilterTask.execute(taskContext, incomingCaseData);

        String returnedCaseDataJson = convertObjectToJsonString(returnedCaseData);
        assertThat(returnedCaseDataJson, hasJsonPath("$.GeneralOrders", hasSize(0)));
    }

    @Test
    public void shouldReturnGeneralOrdersAsNullWhenTheyComeInAsNull_ForDivorceParty() {
        Map<String, Object> caseDataWithNoGeneralOrders = new HashMap<>();
        when(userDivorcePartyLookup.lookupDivorcePartForGivenUser(AUTH_TOKEN, caseDataWithNoGeneralOrders)).thenReturn(Optional.of(RESPONDENT));

        Map<String, Object> returnedCaseData = generalOrdersFilterTask.execute(taskContext, caseDataWithNoGeneralOrders);

        assertThat(returnedCaseData, not(hasKey(GENERAL_ORDERS)));
    }

    @Test
    public void shouldReturnGeneralOrdersAsNullWhenTheyComeInAsNull_ForNotDivorceParty() {
        Map<String, Object> caseDataWithNoGeneralOrders = new HashMap<>();
        when(userDivorcePartyLookup.lookupDivorcePartForGivenUser(AUTH_TOKEN, caseDataWithNoGeneralOrders)).thenReturn(Optional.empty());

        Map<String, Object> returnedCaseData = generalOrdersFilterTask.execute(taskContext, caseDataWithNoGeneralOrders);

        assertThat(returnedCaseData, not(hasKey(GENERAL_ORDERS)));
    }

    @Test
    public void shouldReturnGeneralOrdersAsEmptyLisWhenTheyComeInAsEmptyList_ForDivorceParty() {
        Map<String, Object> caseDataWithNoGeneralOrders = new HashMap<>();
        caseDataWithNoGeneralOrders.put(GENERAL_ORDERS, emptyList());
        when(userDivorcePartyLookup.lookupDivorcePartForGivenUser(AUTH_TOKEN, caseDataWithNoGeneralOrders)).thenReturn(Optional.of(RESPONDENT));

        Map<String, Object> returnedCaseData = generalOrdersFilterTask.execute(taskContext, caseDataWithNoGeneralOrders);

        assertThat(returnedCaseData, hasEntry(GENERAL_ORDERS, emptyList()));
    }

    @Test
    public void shouldReturnGeneralOrdersAsEmptyLisWhenTheyComeInAsEmptyList_ForNotDivorceParty() {
        Map<String, Object> caseDataWithNoGeneralOrders = new HashMap<>();
        caseDataWithNoGeneralOrders.put(GENERAL_ORDERS, emptyList());
        when(userDivorcePartyLookup.lookupDivorcePartForGivenUser(AUTH_TOKEN, caseDataWithNoGeneralOrders)).thenReturn(Optional.empty());

        Map<String, Object> returnedCaseData = generalOrdersFilterTask.execute(taskContext, caseDataWithNoGeneralOrders);

        assertThat(returnedCaseData, hasEntry(GENERAL_ORDERS, emptyList()));
    }

}