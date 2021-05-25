package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.GeneralEmailDetails;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_DETAILS_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_OTHER_RECIPIENT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_PARTIES;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getObjectMapperInstance;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.contextWithToken;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.Formats.CCD_DATE;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateFromLocalDate;

@RunWith(MockitoJUnitRunner.class)
public class StoreGeneralEmailFieldsTaskTest {

    private static final String GENERAL_EMAIL_PARTIES_VALUE = "parties";
    private static final String GENERAL_EMAIL_DETAILS_VALUE = "general email text";
    private static final String GENERAL_EMAIL_OTHER_RECIPIENT_NAME_VALUE = "recipient name";
    private static final String GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL_VALUE = "recipient email";
    private static final String EXPECTED_USERNAME = "test-user";

    @Mock
    private IdamClient idamClient;
    @Mock
    private AuthUtil authUtil;

    private StoreGeneralEmailFieldsTask storeGeneralEmailFieldsTask;

    @Before
    public void setup() {
        storeGeneralEmailFieldsTask = new StoreGeneralEmailFieldsTask(idamClient, authUtil, getObjectMapperInstance());
    }

    @Test
    public void whenTheTaskRuns_thenItAddsGeneralEmailFieldsToTheCollection() {
        when(idamClient.getUserDetails(any())).thenReturn(UserDetails.builder().forename(EXPECTED_USERNAME).build());

        Map<String, Object> result = storeGeneralEmailFieldsTask.execute(contextWithToken(), payload());

        assertThat((Collection<CollectionMember<GeneralEmailDetails>>)result.get(GENERAL_EMAIL_DETAILS_COLLECTION), hasSize(1));
        GeneralEmailDetails generalEmailDetails = ((List<CollectionMember<GeneralEmailDetails>>)result.get(GENERAL_EMAIL_DETAILS_COLLECTION))
            .get(0).getValue();
        assertThat(generalEmailDetails.getGeneralEmailDateTime().substring(0, CCD_DATE.length()), is(formatDateFromLocalDate(LocalDate.now())));
        assertThat(generalEmailDetails.getGeneralEmailParties(), is(GENERAL_EMAIL_PARTIES_VALUE));
        assertThat(generalEmailDetails.getGeneralEmailOtherRecipientEmail(), is(GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL_VALUE));
        assertThat(generalEmailDetails.getGeneralEmailOtherRecipientName(), is(GENERAL_EMAIL_OTHER_RECIPIENT_NAME_VALUE));
        assertThat(generalEmailDetails.getGeneralEmailCreatedBy(), is(EXPECTED_USERNAME));
        assertThat(generalEmailDetails.getGeneralEmailBody(), is(GENERAL_EMAIL_DETAILS_VALUE));
    }

    private Map<String, Object> payload() {
        return ImmutableMap.of(
            GENERAL_EMAIL_PARTIES, GENERAL_EMAIL_PARTIES_VALUE,
            GENERAL_EMAIL_DETAILS, GENERAL_EMAIL_DETAILS_VALUE,
            GENERAL_EMAIL_OTHER_RECIPIENT_NAME, GENERAL_EMAIL_OTHER_RECIPIENT_NAME_VALUE,
            GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL, GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL_VALUE
        );
    }
}
