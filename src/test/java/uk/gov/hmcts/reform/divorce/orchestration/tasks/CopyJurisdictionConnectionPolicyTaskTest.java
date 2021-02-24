package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_JURISDICTION_CONNECTION_POLICY_DIV_SESSION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_LEGAL_CONNECTION_POLICY_DIV_SESSION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.OLD_JURISDICTION_CONNECTION_POLICY_DIV_SESSION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;


@RunWith(MockitoJUnitRunner.class)
public class CopyJurisdictionConnectionPolicyTaskTest {

    @InjectMocks
    private CopyJurisdictionConnectionPolicyTask copyJurisdictionConnectionPolicyTask;

    private static final String D8JURISDICTION_CONNECTION_PAYLOAD = "/jsonExamples/payloads/d8JurisdictionConnection.json";
    private static final String D8JURISDICTION_CONNECTION_NEW_POLICY_PAYLOAD = "/jsonExamples/payloads/d8JurisdictionConnectionNewPolicy.json";
    private Map<String, Object> originalOldPolicyData;
    private Map<String, Object> originalNewPolicyData;
    private TaskContext context;

    @Before
    public void setup() throws IOException {
        originalOldPolicyData = getJsonFromResourceFile(D8JURISDICTION_CONNECTION_PAYLOAD, CcdCallbackRequest.class).getCaseDetails().getCaseData();
        originalNewPolicyData = getJsonFromResourceFile(D8JURISDICTION_CONNECTION_NEW_POLICY_PAYLOAD, CcdCallbackRequest.class)
            .getCaseDetails().getCaseData();

        context = new DefaultTaskContext();
    }

    @Test
    public void executeShouldCopyOldJurisdictionConnectionToNewJurisdictionConnection() throws IOException {
        Map<String, Object> testData = getJsonFromResourceFile(D8JURISDICTION_CONNECTION_PAYLOAD, CcdCallbackRequest.class)
            .getCaseDetails().getCaseData();

        copyJurisdictionConnectionPolicyTask.execute(context, testData);

        assertThat(testData, hasKey(NEW_JURISDICTION_CONNECTION_POLICY_DIV_SESSION));
        assertThat(testData.get(NEW_LEGAL_CONNECTION_POLICY_DIV_SESSION), equalTo(YES_VALUE));
        assertThat(testData.get(NEW_JURISDICTION_CONNECTION_POLICY_DIV_SESSION),
            equalTo(originalOldPolicyData.get(OLD_JURISDICTION_CONNECTION_POLICY_DIV_SESSION)));
    }

    @Test
    public void executeShouldCopyOldJurisdictionConnectionToReplaceExistingNewJurisdictionConnection() throws IOException {
        Map<String, Object> testData = getJsonFromResourceFile(D8JURISDICTION_CONNECTION_NEW_POLICY_PAYLOAD, CcdCallbackRequest.class)
            .getCaseDetails().getCaseData();

        copyJurisdictionConnectionPolicyTask.execute(context, testData);

        assertThat(testData.get(NEW_LEGAL_CONNECTION_POLICY_DIV_SESSION), equalTo(YES_VALUE));
        assertThat(testData.get(NEW_JURISDICTION_CONNECTION_POLICY_DIV_SESSION),
            equalTo(originalNewPolicyData.get(OLD_JURISDICTION_CONNECTION_POLICY_DIV_SESSION)));
        assertThat(testData.get(NEW_JURISDICTION_CONNECTION_POLICY_DIV_SESSION),
            is(not(equalTo(originalNewPolicyData.get(NEW_JURISDICTION_CONNECTION_POLICY_DIV_SESSION)))));
    }
}
