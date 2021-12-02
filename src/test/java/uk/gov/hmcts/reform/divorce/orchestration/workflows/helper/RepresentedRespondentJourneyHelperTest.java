package uk.gov.hmcts.reform.divorce.orchestration.workflows.helper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_DIGITAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.CaseDataTestHelper.buildOrganisationPolicy;

@RunWith(MockitoJUnitRunner.class)
public class RepresentedRespondentJourneyHelperTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private RepresentedRespondentJourneyHelper classUnderTest;

    @Before
    public void setUp() {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(true);
    }

    @Test
    public void shouldGenerateRespondentAosInvitation_WhenRespondentIsNotRepresented() {
        Map<String, Object> caseData = Map.of(
            RESP_SOL_REPRESENTED, NO_VALUE
        );

        boolean result = classUnderTest.shouldGenerateRespondentAosInvitation(caseData);

        assertThat(result, is(true));
    }

    @Test
    public void shouldNotGenerateRespondentAosInvitation_WhenRespondentIsRepresentedByNonDigitalSolicitor_AndToggleIsOn() {
        Map<String, Object> caseData = Map.of(
            RESP_SOL_REPRESENTED, YES_VALUE
        );

        boolean result = classUnderTest.shouldGenerateRespondentAosInvitation(caseData);

        assertThat(result, is(false));
    }

    @Test
    public void shouldGenerateRespondentAosInvitation_WhenRespondentIsRepresentedByNonDigitalSolicitor_AndToggleIsOff() {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(false);
        Map<String, Object> caseData = Map.of(
            RESP_SOL_REPRESENTED, YES_VALUE
        );

        boolean result = classUnderTest.shouldGenerateRespondentAosInvitation(caseData);

        assertThat(result, is(true));
    }

    @Test
    public void shouldGenerateRespondentAosInvitation_WhenRespondentIsRepresentedByDigitalSolicitor() {
        Map<String, Object> caseData = Map.of(
            RESP_SOL_REPRESENTED, YES_VALUE,
            RESPONDENT_SOLICITOR_DIGITAL, YES_VALUE,
            RESPONDENT_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy()
        );

        boolean result = classUnderTest.shouldGenerateRespondentAosInvitation(caseData);

        assertThat(result, is(true));
    }

    @Test
    public void shouldUpdateNoticeOfProceedingsDetails_WhenRespondentSolicitorIsDigital_AndToggleIsOn() {
        Map<String, Object> caseData = Map.of(
            RESPONDENT_SOLICITOR_DIGITAL, YES_VALUE,
            RESPONDENT_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy()
        );

        boolean result = classUnderTest.shouldUpdateNoticeOfProceedingsDetails(caseData);

        assertThat(result, is(true));
    }

    @Test
    public void shouldNotUpdateNoticeOfProceedingsDetails_WhenRespondentSolicitorIsDigital_AndToggleIsOff() {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(false);
        Map<String, Object> caseData = Map.of(
            RESPONDENT_SOLICITOR_DIGITAL, YES_VALUE,
            RESPONDENT_SOLICITOR_ORGANISATION_POLICY, buildOrganisationPolicy()
        );

        boolean result = classUnderTest.shouldUpdateNoticeOfProceedingsDetails(caseData);

        assertThat(result, is(false));
    }

    @Test
    public void shouldNotUpdateNoticeOfProceedingsDetails_WhenRespondentSolicitorIsNotDigital_AndToggleIsOn() {
        Map<String, Object> caseData = Map.of();

        boolean result = classUnderTest.shouldUpdateNoticeOfProceedingsDetails(caseData);

        assertThat(result, is(false));
    }

    @Test
    public void shouldNotUpdateNoticeOfProceedingsDetails_WhenRespondentSolicitorIsNotDigital_AndToggleIsOff() {
        when(featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY)).thenReturn(false);
        Map<String, Object> caseData = Map.of();

        boolean result = classUnderTest.shouldUpdateNoticeOfProceedingsDetails(caseData);

        assertThat(result, is(false));
    }

}