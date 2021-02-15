package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Organisation;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.generics.SolicitorOrganisationPolicyReferenceTask;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8_RESPONDENT_SOLICITOR_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_SOL_REPRESENTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;

@Component
@Slf4j
public class SetRespondentSolicitorOrganisationPolicyReferenceTaskTest extends SolicitorOrganisationPolicyReferenceTaskTest {

    @InjectMocks
    private SetRespondentSolicitorOrganisationPolicyReferenceTask setSolicitorOrgPolicyReferenceTask;

    @Override
    protected SolicitorOrganisationPolicyReferenceTask getTask() {
        return setSolicitorOrgPolicyReferenceTask;
    }

    @Override
    protected String getSolicitorOrganisationPolicyCaseField() {
        return RESPONDENT_SOLICITOR_ORGANISATION_POLICY;
    }

    @Override
    protected String getSolicitorReferenceCaseField() {
        return D8_RESPONDENT_SOLICITOR_REFERENCE;
    }

    @Override
    protected void setCaseDataWithoutOrganisationPolicy() {
        caseData.put(getSolicitorReferenceCaseField(), TEST_SOLICITOR_REFERENCE);
        caseData.put(RESP_SOL_REPRESENTED, YES_VALUE);
    }

    @Override
    protected void setCaseDataSolicitorCreate() {
        setCaseDataWithoutOrganisationPolicy();
        caseData.put(getSolicitorOrganisationPolicyCaseField(), OrganisationPolicy.builder()
            .organisation(Organisation.builder().build())
            .build());
    }

    @Override
    protected void setCaseDataWithoutSolicitorReference() {
        caseData.put("SomeOtherKey", "SomeOtherValue");
    }

    @Override
    protected void setCaseDataSolicitorUpdate() {
        setCaseDataWithoutOrganisationPolicy();
        caseData.put(getSolicitorOrganisationPolicyCaseField(), OrganisationPolicy.builder()
            .orgPolicyReference("ExistingPolicyReference")
            .organisation(Organisation.builder()
                .build())
            .build());
    }

    @Test
    public void shouldNotMapSolicitorReferenceWhenNotRepresented() {
        setCaseDataWithoutSolicitorReference();
        caseData.put(RESP_SOL_REPRESENTED, NO_VALUE);

        Map<String, Object> returnCaseData = getTask().execute(context, caseData);

        OrganisationPolicy organisationPolicy = (OrganisationPolicy) returnCaseData.get(getSolicitorOrganisationPolicyCaseField());

        assertThat(organisationPolicy, is(nullValue()));
        assertThat(caseData, is(returnCaseData));
    }

}