package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractorTest.DA_GRANTED_CERTIFICATE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractorTest.buildCaseDataWithDocumentsGeneratedList;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractorTest.buildCollectionMemberWithDocumentType;

@RunWith(MockitoJUnitRunner.class)
public class AddDaGrantedCertificateToDocumentsToPrintTaskTest {

    @InjectMocks
    private AddDaGrantedCertificateToDocumentsToPrintTask addDaGrantedCertificateToDocumentsToPrintTask;

    @Test
    public void executeAddsAnotherDocumentToContext() {
        TaskContext context = new DefaultTaskContext();

        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(
            asList(
                buildCollectionMemberWithDocumentType(DA_GRANTED_CERTIFICATE)
            )
        );

        addDaGrantedCertificateToDocumentsToPrintTask.execute(context, caseData);

        List<GeneratedDocumentInfo> documentsToBulkPrint = context.getTransientObject(
            PrepareDataForDocumentGenerationTask.ContextKeys.GENERATED_DOCUMENTS
        );

        assertThat(documentsToBulkPrint.size(), is(1));
    }
}
