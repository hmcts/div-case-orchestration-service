package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.exception.JudgeTypeNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDERS;

@RunWith(MockitoJUnitRunner.class)
public class GeneralOrderGenerationTaskTest extends AbstractGeneralOrderGenerationTaskTest {

    private final Map<String, Object> modifiedCaseData = Collections.singletonMap("modifiedKey", "modifiedValue");
    @InjectMocks
    private GeneralOrderGenerationTask generalOrderGenerationTask;
    @Captor
    private ArgumentCaptor<GeneratedDocumentInfo> newDocumentCaptor;

    @Before
    public void setup() throws JudgeTypeNotFoundException {
        super.setup();
        when(ccdUtil.addNewDocumentToCollection(any(), any(), eq(GENERAL_ORDERS))).thenReturn(modifiedCaseData);
    }

    @Test
    public void testExecuteShouldGenerateAFile() throws TaskException {
        Map<String, Object> caseData = executeShouldGenerateAFile();

        assertThat(caseData, is(modifiedCaseData));
    }

    @Override
    public GeneralOrderGenerationTask getTask() {
        return generalOrderGenerationTask;
    }

    @Override
    protected void runVerifications(Map<String, Object> expectedIncomingCaseData,
                                    Map<String, Object> returnedCaseData,
                                    String expectedDocumentType,
                                    String expectedTemplateId,
                                    DocmosisTemplateVars expectedDocmosisTemplateVars) {
        assertThat(returnedCaseData, equalTo(modifiedCaseData));
        verifyNewDocumentWasAddedToCaseData(expectedIncomingCaseData, expectedDocumentType);
        verifyPdfDocumentGenerationCallIsCorrect(expectedTemplateId, expectedDocmosisTemplateVars);
    }

    private void verifyNewDocumentWasAddedToCaseData(Map<String, Object> expectedIncomingCaseData, String expectedDocumentType) {
        verify(ccdUtil).addNewDocumentToCollection(eq(expectedIncomingCaseData), newDocumentCaptor.capture(), eq(GENERAL_ORDERS));
        GeneratedDocumentInfo generatedDocumentInfo = newDocumentCaptor.getValue();

        assertThat(generatedDocumentInfo.getDocumentType(), is(expectedDocumentType));
        assertThat(generatedDocumentInfo.getFileName(), is(newGeneratedDocument.getFileName()));
    }
}
