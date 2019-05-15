package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CERTIFICATE_OF_ENTITLEMENT_FILENAME_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CERTIFICATE_OF_ENTITLEMENT;

@Component
public class CertificateOfEntitlementGenerator implements Task<Map<String, Object>> {
    private final DocumentGeneratorClient documentGeneratorClient;

    @Autowired
    public CertificateOfEntitlementGenerator(DocumentGeneratorClient documentGeneratorClient) {
        this.documentGeneratorClient = documentGeneratorClient;
    }

    @Override
    @SuppressWarnings("Duplicates")
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) throws TaskException {
        try {
            final CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

            GeneratedDocumentInfo certificateOfEntitlement =
                    documentGeneratorClient.generatePDF(
                            GenerateDocumentRequest.builder()
                                    .template(CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_NAME)
                                    .values(Collections.singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY,
                                            caseDetails))
                                    .build(),
                            context.getTransientObject(AUTH_TOKEN_JSON_KEY)
                    );

            certificateOfEntitlement.setDocumentType(DOCUMENT_TYPE_CERTIFICATE_OF_ENTITLEMENT);
            certificateOfEntitlement.setFileName(String.format(CERTIFICATE_OF_ENTITLEMENT_FILENAME_FORMAT,
                    caseDetails.getCaseId()));

            final LinkedHashSet<GeneratedDocumentInfo> documentCollection =
                    context.computeTransientObjectIfAbsent(DOCUMENT_COLLECTION, new LinkedHashSet<>());
            documentCollection.add(certificateOfEntitlement);

            return caseData;
        } catch (Exception e) {
            throw new TaskException("Unable to generate or store Certificate of Entitlement.", e);
        }
    }
}
