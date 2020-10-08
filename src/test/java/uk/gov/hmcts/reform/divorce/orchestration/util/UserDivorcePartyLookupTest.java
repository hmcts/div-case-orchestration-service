package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.model.parties.DivorceParty;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.PETITIONER;
import static uk.gov.hmcts.reform.divorce.model.parties.DivorceParty.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_EMAIL_ADDRESS;

@RunWith(MockitoJUnitRunner.class)
public class UserDivorcePartyLookupTest {

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private UserDivorcePartyLookup userDivorcePartyLookup;

    private Map<String, Object> caseData;

    @Before
    public void setUp() {
        caseData = Map.of(
            D_8_PETITIONER_EMAIL, TEST_PETITIONER_EMAIL,
            RESPONDENT_EMAIL_ADDRESS, TEST_RESPONDENT_EMAIL,
            CO_RESP_EMAIL_ADDRESS, TEST_CO_RESPONDENT_EMAIL
        );
    }

    @Test
    public void shouldReturnUserAsPetitionerWhenEmailMatchesPetitionerEmail() {
        when(idamClient.getUserDetails(AUTH_TOKEN)).thenReturn(UserDetails.builder().email(TEST_PETITIONER_EMAIL).build());

        Optional<DivorceParty> divorceParty = userDivorcePartyLookup.lookupDivorcePartForGivenUser(AUTH_TOKEN, caseData);

        assertThat(divorceParty.isPresent(), is(true));
        assertThat(divorceParty.get(), is(PETITIONER));
    }

    @Test
    public void shouldReturnUserAsRespondentWhenEmailMatchesRespondentEmail() {
        when(idamClient.getUserDetails(AUTH_TOKEN)).thenReturn(UserDetails.builder().email(TEST_RESPONDENT_EMAIL).build());

        Optional<DivorceParty> divorceParty = userDivorcePartyLookup.lookupDivorcePartForGivenUser(AUTH_TOKEN, caseData);

        assertThat(divorceParty.isPresent(), is(true));
        assertThat(divorceParty.get(), is(RESPONDENT));
    }

    @Test
    public void shouldReturnUserAsCoRespondentWhenEmailMatchesCoRespondentEmail() {
        when(idamClient.getUserDetails(AUTH_TOKEN)).thenReturn(UserDetails.builder().email(TEST_CO_RESPONDENT_EMAIL).build());

        Optional<DivorceParty> divorceParty = userDivorcePartyLookup.lookupDivorcePartForGivenUser(AUTH_TOKEN, caseData);

        assertThat(divorceParty.isPresent(), is(true));
        assertThat(divorceParty.get(), is(CO_RESPONDENT));
    }

    @Test
    public void shouldReturnUserDivorcePartyAsEmptyWhenEmailMatchesNoneOfCitizenEmailAddresses() {
        when(idamClient.getUserDetails(AUTH_TOKEN)).thenReturn(UserDetails.builder().email(TEST_USER_EMAIL).build());

        Optional<DivorceParty> divorceParty = userDivorcePartyLookup.lookupDivorcePartForGivenUser(AUTH_TOKEN, caseData);

        assertThat(divorceParty.isPresent(), is(false));
    }

}