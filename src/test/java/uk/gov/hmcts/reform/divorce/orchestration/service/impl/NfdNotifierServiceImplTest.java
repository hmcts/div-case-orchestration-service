package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.util.SearchForCaseByEmail;
import uk.gov.hmcts.reform.divorce.orchestration.util.nfd.IdamUser;
import uk.gov.hmcts.reform.divorce.orchestration.util.nfd.IdamUsersCsvLoader;
import uk.gov.hmcts.reform.divorce.orchestration.util.nfd.NfdAuthUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.nfd.NfdIdamService;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.service.impl.NfdNotifierServiceImpl.EMAIL_DESCRIPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.service.impl.NfdNotifierServiceImpl.FIRSTNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.impl.NfdNotifierServiceImpl.LASTNAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.impl.NfdNotifierServiceImpl.SUBJECT;

@RunWith(MockitoJUnitRunner.class)
public class NfdNotifierServiceImplTest {

    protected static final String AUTH_TOKEN = "authToken";
    protected static final String EMAIL = "test@email.com";
    protected static final String USER_ID = "userId";
    protected static final String FORENAME = "John";
    protected static final String SURNAME = "Doe";
    NfdNotifierServiceImpl notifierService;
    @Captor
    ArgumentCaptor<Map<String, String>> templateMapCaptor;
    @Captor
    ArgumentCaptor<String> emailArgumentCaptor;
    @Captor
    ArgumentCaptor<String> templateNameArgumentCaptor;
    @Captor
    ArgumentCaptor<String> descriptionCaptor;
    @Mock
    private EmailService emailService;
    @Mock
    private SearchForCaseByEmail searchForCaseByEmail;
    @Mock
    private IdamUsersCsvLoader csvLoader;
    @Mock
    private NfdIdamService nfdIdamService;
    @Mock
    private NfdAuthUtil nfdAuthUtil;

    @Before
    public void setUpTest() {
        when(nfdAuthUtil.getCaseworkerToken()).thenReturn(AUTH_TOKEN);
        when(csvLoader.loadIdamUserList("unsubmittedIdamUserList.csv")).thenReturn(Arrays.asList(IdamUser.builder().idamId(USER_ID).build()));
        when(nfdIdamService.getUserDetail(USER_ID, AUTH_TOKEN)).thenReturn(
            UserDetails.builder().email(EMAIL).forename(FORENAME).surname(SURNAME).build());
    }

    @Test
    public void shouldNotifyUsers() throws CaseOrchestrationServiceException {
        notifierService = new NfdNotifierServiceImpl(emailService, searchForCaseByEmail, csvLoader, nfdIdamService, nfdAuthUtil);

        when(searchForCaseByEmail.searchCasesByEmail(EMAIL)).thenReturn(Optional.empty());
        notifierService.notifyUnsubmittedApplications();

        Mockito.verify(emailService)
            .sendEmail(emailArgumentCaptor.capture(), templateNameArgumentCaptor.capture(), templateMapCaptor.capture(), descriptionCaptor.capture(),
                any(LanguagePreference.class));

        assertThat(EMAIL, equalTo(emailArgumentCaptor.getValue()));
        assertThat(EmailTemplateNames.NFD_NOTIFICATION.name(), equalTo(templateNameArgumentCaptor.getValue()));
        assertThat(EMAIL_DESCRIPTION, equalTo(descriptionCaptor.getValue()));


        Map<String, String> templateMap = templateMapCaptor.getValue();
        assertThat(templateMap.get(FIRSTNAME), equalTo(FORENAME));
        assertThat(templateMap.get(LASTNAME), equalTo(SURNAME));
        assertThat(templateMap.get(SUBJECT), equalTo("Submit your divorce application"));
    }

}