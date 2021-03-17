package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Organisation;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_ORGANISATION_POLICY_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.NOTICE_OF_PROCEEDINGS_DIGITAL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.NOTICE_OF_PROCEEDINGS_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.NOTICE_OF_PROCEEDINGS_FIRM;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.EmailDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor.CaseDataKeys.RESPONDENT_SOLICITOR_ORGANISATION;

@RunWith(MockitoJUnitRunner.class)
public class UpdateNoticeOfProceedingsDetailsTaskTest {

    @InjectMocks
    private UpdateNoticeOfProceedingsDetailsTask target;

    @Test
    public void updateNoticeOfProceedingsDetailsTaskTest() {
        final TaskContext context = new DefaultTaskContext();
        Map<String, Object> caseData = new HashMap();
        caseData.put(RESPONDENT_SOLICITOR_EMAIL, TEST_USER_EMAIL);
        caseData.put(RESPONDENT_SOLICITOR_ORGANISATION, buildOrganisationPolicyData());

        target.execute(context, caseData);

        Assert.assertEquals(caseData.get(NOTICE_OF_PROCEEDINGS_DIGITAL), YES_VALUE);
        Assert.assertEquals(caseData.get(NOTICE_OF_PROCEEDINGS_EMAIL), TEST_USER_EMAIL);
        Assert.assertEquals(caseData.get(NOTICE_OF_PROCEEDINGS_FIRM), TEST_ORGANISATION_POLICY_NAME);
    }

    private OrganisationPolicy buildOrganisationPolicyData() {
        return OrganisationPolicy.builder()
            .orgPolicyReference("ref")
            .organisation(Organisation
                .builder()
                .organisationID("id")
                .organisationName(TEST_ORGANISATION_POLICY_NAME)
                .build())
            .build();
    }
}