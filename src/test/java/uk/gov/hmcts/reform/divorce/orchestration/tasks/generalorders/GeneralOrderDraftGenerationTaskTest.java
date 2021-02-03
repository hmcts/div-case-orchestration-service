package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDER_DRAFT;

@RunWith(MockitoJUnitRunner.class)
public class GeneralOrderDraftGenerationTaskTest extends AbstractGeneralOrderGenerationTaskTest {

    @InjectMocks
    private GeneralOrderDraftGenerationTask generalOrderDraftGenerationTask;

    @Test
    public void testExecuteShouldGenerateAFile() throws TaskException {
        Map<String, Object> caseData = executeShouldGenerateAFile();

        assertThat(caseData, hasKey(GENERAL_ORDER_DRAFT));
    }

    @Override
    public GeneralOrderGenerationTask getTask() {
        return generalOrderDraftGenerationTask;
    }

    @Override
    protected void runVerifications(Map<String, Object> expectedIncomingCaseData,
                                    Map<String, Object> returnedCaseData,
                                    String expectedDocumentType,
                                    String expectedTemplateId,
                                    DocmosisTemplateVars expectedDocmosisTemplateVars) {
        verifyDraftAddedToCaseData(expectedIncomingCaseData);
        verifyPdfDocumentGenerationCallIsCorrect(expectedTemplateId, expectedDocmosisTemplateVars);
    }

    private void verifyDraftAddedToCaseData(Map<String, Object> expectedIncomingCaseData) {
        DocumentLink generatedDocumentInfo = (DocumentLink) expectedIncomingCaseData.get(GENERAL_ORDER_DRAFT);

        assertThat(generatedDocumentInfo.getDocumentFilename(), is(newGeneratedDocument.getFileName() + ".pdf"));
    }

}
