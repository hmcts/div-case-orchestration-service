package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CO_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_OTHER_PARTY_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_FULL_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CO_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CCD_REFERENCE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CO_RESPONDENT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_CO_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_OTHER_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_PET_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_RESP_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NOTIFICATION_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.CO_RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.OTHER_PARTY_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.CO_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.CO_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.OTHER;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.PETITIONER;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.PETITIONER_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.GeneralEmailTaskHelper.Party.RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.TaskContextHelper.context;

public class GeneralEmailTaskHelperTest {

    @Test
    public void getExpectedNotificationTemplateVars_forPetitioner() {
        Map<String, String> templateVars = GeneralEmailTaskHelper.getExpectedNotificationTemplateVars(
            PETITIONER, context(), buildCaseData()
        );

        assertThat(templateVars.get(NOTIFICATION_CCD_REFERENCE_KEY), is(TEST_CASE_ID));
        assertThat(templateVars.get(NOTIFICATION_GENERAL_EMAIL_DETAILS), is(TEST_GENERAL_EMAIL_DETAILS));
        assertThat(templateVars.get(NOTIFICATION_PET_NAME), is(TEST_PETITIONER_FULL_NAME));
    }

    @Test
    public void getExpectedNotificationTemplateVars_forPetitionerSolicitor() {
        Map<String, String> templateVars = GeneralEmailTaskHelper.getExpectedNotificationTemplateVars(
            PETITIONER_SOLICITOR, context(), buildCaseData()
        );

        assertThat(templateVars.get(NOTIFICATION_CCD_REFERENCE_KEY), is(TEST_CASE_ID));
        assertThat(templateVars.get(NOTIFICATION_GENERAL_EMAIL_DETAILS), is(TEST_GENERAL_EMAIL_DETAILS));

        assertThat(templateVars.get(NOTIFICATION_PET_NAME), is(TEST_PETITIONER_FULL_NAME));
        assertThat(templateVars.get(NOTIFICATION_RESP_NAME), is(TEST_RESPONDENT_FULL_NAME));
        assertThat(templateVars.get(NOTIFICATION_SOLICITOR_NAME), is(TEST_SOLICITOR_NAME));
    }

    @Test
    public void getExpectedNotificationTemplateVars_forRespondent() {
        Map<String, String> templateVars = GeneralEmailTaskHelper.getExpectedNotificationTemplateVars(
            RESPONDENT, context(), buildCaseData()
        );

        assertThat(templateVars.get(NOTIFICATION_CCD_REFERENCE_KEY), is(TEST_CASE_ID));
        assertThat(templateVars.get(NOTIFICATION_GENERAL_EMAIL_DETAILS), is(TEST_GENERAL_EMAIL_DETAILS));
        assertThat(templateVars.get(NOTIFICATION_RESP_NAME), is(TEST_RESPONDENT_FULL_NAME));
    }

    @Test
    public void getExpectedNotificationTemplateVars_forRespondentSolicitor() {
        Map<String, String> templateVars = GeneralEmailTaskHelper.getExpectedNotificationTemplateVars(
            RESPONDENT_SOLICITOR, context(), buildCaseData()
        );

        assertThat(templateVars.get(NOTIFICATION_CCD_REFERENCE_KEY), is(TEST_CASE_ID));
        assertThat(templateVars.get(NOTIFICATION_GENERAL_EMAIL_DETAILS), is(TEST_GENERAL_EMAIL_DETAILS));

        assertThat(templateVars.get(NOTIFICATION_PET_NAME), is(TEST_PETITIONER_FULL_NAME));
        assertThat(templateVars.get(NOTIFICATION_RESP_NAME), is(TEST_RESPONDENT_FULL_NAME));
        assertThat(templateVars.get(NOTIFICATION_RESPONDENT_SOLICITOR_NAME), is(TEST_RESPONDENT_SOLICITOR_NAME));
    }

    @Test
    public void getExpectedNotificationTemplateVars_forCoRespondent() {
        Map<String, String> templateVars = GeneralEmailTaskHelper.getExpectedNotificationTemplateVars(
            CO_RESPONDENT, context(), buildCaseData()
        );

        assertThat(templateVars.get(NOTIFICATION_CCD_REFERENCE_KEY), is(TEST_CASE_ID));
        assertThat(templateVars.get(NOTIFICATION_GENERAL_EMAIL_DETAILS), is(TEST_GENERAL_EMAIL_DETAILS));
        assertThat(templateVars.get(NOTIFICATION_CO_RESPONDENT_NAME), is(TEST_CO_RESPONDENT_FULL_NAME));
    }

    @Test
    public void getExpectedNotificationTemplateVars_forCoRespondentSolicitor() {
        Map<String, String> templateVars = GeneralEmailTaskHelper.getExpectedNotificationTemplateVars(
            CO_RESPONDENT_SOLICITOR, context(), buildCaseData()
        );

        assertThat(templateVars.get(NOTIFICATION_CCD_REFERENCE_KEY), is(TEST_CASE_ID));
        assertThat(templateVars.get(NOTIFICATION_GENERAL_EMAIL_DETAILS), is(TEST_GENERAL_EMAIL_DETAILS));

        assertThat(templateVars.get(NOTIFICATION_PET_NAME), is(TEST_PETITIONER_FULL_NAME));
        assertThat(templateVars.get(NOTIFICATION_RESP_NAME), is(TEST_RESPONDENT_FULL_NAME));
        assertThat(templateVars.get(NOTIFICATION_CO_RESPONDENT_SOLICITOR_NAME), is(TEST_CO_RESPONDENT_SOLICITOR_NAME));
    }

    @Test
    public void getExpectedNotificationTemplateVars_forOtherParty() {
        Map<String, String> templateVars = GeneralEmailTaskHelper.getExpectedNotificationTemplateVars(
            OTHER, context(), buildCaseData()
        );

        assertThat(templateVars.get(NOTIFICATION_CCD_REFERENCE_KEY), is(TEST_CASE_ID));
        assertThat(templateVars.get(NOTIFICATION_GENERAL_EMAIL_DETAILS), is(TEST_GENERAL_EMAIL_DETAILS));
        assertThat(templateVars.get(NOTIFICATION_PET_NAME), is(TEST_PETITIONER_FULL_NAME));
        assertThat(templateVars.get(NOTIFICATION_RESP_NAME), is(TEST_RESPONDENT_FULL_NAME));
        assertThat(templateVars.get(NOTIFICATION_OTHER_NAME), is(TEST_OTHER_PARTY_NAME));
    }


    private Map<String, Object> buildCaseData() {
        Map<String, Object> caseData = new HashMap<>();

        caseData.put(PETITIONER_FIRST_NAME, TEST_PETITIONER_FIRST_NAME);
        caseData.put(PETITIONER_LAST_NAME, TEST_PETITIONER_LAST_NAME);
        caseData.put(FullNamesDataExtractor.CaseDataKeys.PETITIONER_SOLICITOR_NAME, TEST_SOLICITOR_NAME);
        caseData.put(RESPONDENT_FIRST_NAME, TEST_RESPONDENT_FIRST_NAME);
        caseData.put(RESPONDENT_LAST_NAME, TEST_RESPONDENT_LAST_NAME);
        caseData.put(RESPONDENT_SOLICITOR_NAME, TEST_RESPONDENT_SOLICITOR_NAME);
        caseData.put(CO_RESPONDENT_FIRST_NAME, TEST_CO_RESPONDENT_FIRST_NAME);
        caseData.put(CO_RESPONDENT_LAST_NAME, TEST_CO_RESPONDENT_LAST_NAME);
        caseData.put(CO_RESPONDENT_SOLICITOR_NAME, TEST_CO_RESPONDENT_SOLICITOR_NAME);
        caseData.put(OTHER_PARTY_NAME, TEST_OTHER_PARTY_NAME);
        caseData.put(GENERAL_EMAIL_DETAILS, TEST_GENERAL_EMAIL_DETAILS);

        return caseData;

    }
}

