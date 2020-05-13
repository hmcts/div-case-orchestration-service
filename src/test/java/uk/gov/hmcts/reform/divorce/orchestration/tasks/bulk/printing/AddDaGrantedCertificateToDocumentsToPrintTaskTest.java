package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractorTest.DA_GRANTED_CERTIFICATE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractorTest.buildCaseDataWithDocumentsGeneratedList;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedCertificateDataExtractorTest.buildCollectionMemberWithDocumentType;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.PrepareDataForDocumentGenerationTask.appendAnotherDocumentToBulkPrint;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.PrepareDataForDocumentGenerationTaskTest.document;

@RunWith(MockitoJUnitRunner.class)
public class AddDaGrantedCertificateToDocumentsToPrintTaskTest {

    @InjectMocks
    private AddDaGrantedCertificateToDocumentsToPrintTask addDaGrantedCertificateToDocumentsToPrintTask;

    @Test
    public void executeAddsFirstDocumentToContext() {
        TaskContext context = new DefaultTaskContext();

        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(
            asList(
                buildCollectionMemberWithDocumentType(DA_GRANTED_CERTIFICATE)
            )
        );

        addDaGrantedCertificateToDocumentsToPrintTask.execute(context, caseData);

        Map<String, GeneratedDocumentInfo> documentsToBulkPrint = PrepareDataForDocumentGenerationTask.getDocumentToBulkPrint(context);
        GeneratedDocumentInfo document = documentsToBulkPrint.get(DA_GRANTED_CERTIFICATE);

        assertThat(documentsToBulkPrint.size(), is(1));
        assertThat(document, instanceOf(GeneratedDocumentInfo.class));
        assertThat(document.getDocumentType(), is(DA_GRANTED_CERTIFICATE));
    }

    @Test
    public void executeAddsAnotherDocumentToContext() {
        TaskContext context = new DefaultTaskContext();
        final GeneratedDocumentInfo existingDocument = document();
        appendAnotherDocumentToBulkPrint(context, existingDocument);

        Map<String, Object> caseData = buildCaseDataWithDocumentsGeneratedList(
            asList(
                buildCollectionMemberWithDocumentType(DA_GRANTED_CERTIFICATE)
            )
        );

        addDaGrantedCertificateToDocumentsToPrintTask.execute(context, caseData);

        Map<String, GeneratedDocumentInfo> documentsToBulkPrint = PrepareDataForDocumentGenerationTask.getDocumentToBulkPrint(context);
        final GeneratedDocumentInfo document = documentsToBulkPrint.get(DA_GRANTED_CERTIFICATE);

        assertThat(documentsToBulkPrint.size(), is(2));
        assertThat(documentsToBulkPrint.get(existingDocument.getDocumentType()), instanceOf(GeneratedDocumentInfo.class));
        assertThat(documentsToBulkPrint.get(existingDocument.getDocumentType()).getDocumentType(), is(existingDocument.getDocumentType()));
        assertThat(document, instanceOf(GeneratedDocumentInfo.class));
        assertThat(document.getDocumentType(), is(DA_GRANTED_CERTIFICATE));
    }
}
