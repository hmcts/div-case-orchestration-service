package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseFormatterClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.D_8_DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_DOC_URL;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.TEST_STATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.MINI_PETITION_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESPONDENT_INVITATION_TEMPLATE_NAME;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataFormatterTest {
    private CaseDataFormatter caseDataFormatter;

    @Mock
    private CaseFormatterClient caseFormatterClient;
    private Map<String, Object> payload;
    private CaseDetails caseDetails;
    private TaskContext context;
    private GeneratedDocumentInfo aosinvitation;
    private GeneratedDocumentInfo petition;

    @Before
    public void setUp() {
        caseDataFormatter = new CaseDataFormatter(caseFormatterClient);

        petition =
                GeneratedDocumentInfo.builder()
                        .fileName(TEST_FILENAME)
                        .url(TEST_DOC_URL)
                        .build();

        aosinvitation =
                GeneratedDocumentInfo.builder()
                        .fileName(TEST_FILENAME)
                        .url(TEST_DOC_URL)
                        .build();

        payload = new HashMap<>();
        payload.put(MINI_PETITION_TEMPLATE_NAME, petition);
        payload.put(RESPONDENT_INVITATION_TEMPLATE_NAME, aosinvitation);
        payload.put(D_8_DOCUMENTS_GENERATED, Arrays.asList(petition, aosinvitation) );

        caseDetails = CaseDetails.builder()
                .caseId(TEST_CASE_ID)
                .state(TEST_STATE)
                .caseData(payload)
                .build();

        context = new DefaultTaskContext();

    }

    @SuppressWarnings("unchecked")
    @Test
    public void executeShouldReturnUpdatedPayloadForValidCase() throws TaskException {
        //given
        when(caseFormatterClient.addDocuments(any())).thenReturn(payload);

        //when
        Map<String, Object> response = caseDataFormatter.execute(context, payload);

        //then
        assertNotNull(response);
        assertNotNull(response.get(D_8_DOCUMENTS_GENERATED));
        List<GeneratedDocumentInfo> generatedDocumentInfos
                = (List<GeneratedDocumentInfo>) response.get(D_8_DOCUMENTS_GENERATED);
        assertEquals(generatedDocumentInfos.size(), 2);
        assertEquals(generatedDocumentInfos.get(0).getFileName(), TEST_FILENAME);
    }

    @After
    public void tearDown() {
        caseDataFormatter = null;
    }

}