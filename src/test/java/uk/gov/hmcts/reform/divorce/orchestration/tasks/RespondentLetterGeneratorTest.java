package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DOC_URL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.INVITATION_FILE_NAME_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PIN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_INVITATION_TEMPLATE_NAME;

@RunWith(MockitoJUnitRunner.class)
public class RespondentLetterGeneratorTest {
    private RespondentLetterGenerator respondentLetterGenerator;

    @Mock
    private DocumentGeneratorClient documentGeneratorClient;
    private Map<String, Object> payload;
    private CaseDetails caseDetails;
    private TaskContext context;
    private GeneratedDocumentInfo aosinvitation;

    @Before
    public void setUp() {
        respondentLetterGenerator = new RespondentLetterGenerator(documentGeneratorClient);

        aosinvitation =
                GeneratedDocumentInfo.builder()
                        .fileName(TEST_FILENAME)
                        .url(TEST_DOC_URL)
                        .build();

        payload = new HashMap<>();
        payload.put(PIN,TEST_PIN );

        caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .caseData(payload)
                .build();

        context = new DefaultTaskContext();

    }

    @Test
    public void executeShouldReturnUpdatedPayloadForValidCase() throws TaskException {
        //given
        when(documentGeneratorClient.generatePDF(any(), anyString())).thenReturn(aosinvitation);

        //when
        Map<String, Object> response = respondentLetterGenerator.execute(context, payload, AUTH_TOKEN, caseDetails);

        //then
        assertNotNull(response);
        assertEquals(aosinvitation.getDocumentType(), DOCUMENT_TYPE_INVITATION);
        assertEquals(aosinvitation.getFileName(), String.format(INVITATION_FILE_NAME_FORMAT,
                caseDetails.getCaseId()));
        assertTrue(payload.containsKey(RESPONDENT_INVITATION_TEMPLATE_NAME));
    }

    @After
    public void tearDown() {
        respondentLetterGenerator = null;
    }

}