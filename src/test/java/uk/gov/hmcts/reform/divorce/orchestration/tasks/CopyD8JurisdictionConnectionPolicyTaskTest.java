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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.NEW_JURISDICTION_CONNECTION_POLICY_CCD_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.OLD_JURISDICTION_CONNECTION_POLICY_CCD_DATA;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;


@RunWith(MockitoJUnitRunner.class)
public class CopyD8JurisdictionConnectionPolicyTaskTest {

    @InjectMocks
    private CopyD8JurisdictionConnectionPolicyTask copyD8JurisdictionConnectionPolicyTask;

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
    public void executeShouldCopyNewD8JurisdictionConnectionToOldD8JurisdictionConnection() throws IOException {
        Map<String, Object> testData = getJsonFromResourceFile(D8JURISDICTION_CONNECTION_NEW_POLICY_PAYLOAD, CcdCallbackRequest.class)
            .getCaseDetails().getCaseData();

        copyD8JurisdictionConnectionPolicyTask.execute(context, testData);

        assertThat(testData, hasKey(OLD_JURISDICTION_CONNECTION_POLICY_CCD_DATA));
        assertThat(testData.get(OLD_JURISDICTION_CONNECTION_POLICY_CCD_DATA),
            equalTo(originalNewPolicyData.get(NEW_JURISDICTION_CONNECTION_POLICY_CCD_DATA)));
    }

    @Test
    public void executeShouldCopyNewD8JurisdictionConnectionToReplaceExistingOldD8JurisdictionConnection() throws IOException {
        Map<String, Object> testData = getJsonFromResourceFile(D8JURISDICTION_CONNECTION_NEW_POLICY_PAYLOAD, CcdCallbackRequest.class)
            .getCaseDetails().getCaseData();

        copyD8JurisdictionConnectionPolicyTask.execute(context, testData);

        assertThat(testData.get(OLD_JURISDICTION_CONNECTION_POLICY_CCD_DATA),
            equalTo(originalNewPolicyData.get(NEW_JURISDICTION_CONNECTION_POLICY_CCD_DATA)));
        assertThat(testData.get(OLD_JURISDICTION_CONNECTION_POLICY_CCD_DATA),
            is(not(equalTo(originalOldPolicyData.get(OLD_JURISDICTION_CONNECTION_POLICY_CCD_DATA)))));
    }

    @Test
    public void executeShouldNotCopyNewJurisdictionIfNull() throws IOException {
        Map<String, Object> testData = getJsonFromResourceFile(D8JURISDICTION_CONNECTION_NEW_POLICY_PAYLOAD, CcdCallbackRequest.class)
            .getCaseDetails().getCaseData();
        testData.put(NEW_JURISDICTION_CONNECTION_POLICY_CCD_DATA, null);

        copyD8JurisdictionConnectionPolicyTask.execute(context, testData);

        assertThat(testData, not(hasKey(OLD_JURISDICTION_CONNECTION_POLICY_CCD_DATA)));
    }

    @Test
    public void executeShouldNotCopyNewJurisdictionIfNotPresent() throws IOException {
        Map<String, Object> testData = getJsonFromResourceFile(D8JURISDICTION_CONNECTION_NEW_POLICY_PAYLOAD, CcdCallbackRequest.class)
            .getCaseDetails().getCaseData();
        testData.remove(NEW_JURISDICTION_CONNECTION_POLICY_CCD_DATA);

        copyD8JurisdictionConnectionPolicyTask.execute(context, testData);

        assertThat(testData, not(hasKey(OLD_JURISDICTION_CONNECTION_POLICY_CCD_DATA)));
    }
}
