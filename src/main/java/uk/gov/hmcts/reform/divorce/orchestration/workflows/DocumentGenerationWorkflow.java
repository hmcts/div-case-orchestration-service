package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentTypeHelper;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetFormattedDnCourtDetails;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;

@Slf4j
@Component
public class DocumentGenerationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SetFormattedDnCourtDetails setFormattedDnCourtDetails;

    private final DocumentGenerationTask documentGenerationTask;

    private final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    @Autowired
    public DocumentGenerationWorkflow(final SetFormattedDnCourtDetails setFormattedDnCourtDetails,
                                      final DocumentGenerationTask documentGenerationTask,
                                      final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask) {
        this.setFormattedDnCourtDetails = setFormattedDnCourtDetails;
        this.documentGenerationTask = documentGenerationTask;
        this.addNewDocumentsToCaseDataTask = addNewDocumentsToCaseDataTask;
    }

    public Map<String, Object> run(final CaseDetails caseDetails,
                                   final String authToken,
                                   final String ccdDocumentType,
                                   final String defaultTemplate,
                                   final String templateLogicalName,
                                   final String fileName) throws WorkflowException {

        String template = deduceTemplateFromLogicalName(templateLogicalName, caseDetails, defaultTemplate);

        return executeTasks(caseDetails, authToken, ccdDocumentType, template, fileName);
    }

    public Map<String, Object> run(final CaseDetails caseDetails,
                                   final String authToken,
                                   final String ccdDocumentType,
                                   final DocumentType documentType,
                                   final String fileName) throws WorkflowException {

        String template = getLanguageAppropriateTemplate(caseDetails, documentType);

        return executeTasks(caseDetails, authToken, ccdDocumentType, template, fileName);
    }

    private String getLanguageAppropriateTemplate(CaseDetails caseDetails, DocumentType documentType) {
        return DocumentTypeHelper.getLanguageAppropriateTemplate(caseDetails.getCaseData(), documentType);
    }

    private String deduceTemplateFromLogicalName(String templateLogicalName, CaseDetails caseDetails, String defaultTemplate) {
        return DocumentType.getDocumentTypeByTemplateLogicalName(templateLogicalName)
            .map(documentType -> {
                log.info("Found registered document type for {}", templateLogicalName);
                return getLanguageAppropriateTemplate(caseDetails, documentType);
            }).orElseGet(() -> {
                log.warn("Did not find registered document type for {}. Returning the given defaultTemplate [{}]",
                    templateLogicalName, defaultTemplate);
                return defaultTemplate;
            });
    }

    private Map<String, Object> executeTasks(final CaseDetails caseDetails,
                                             final String authToken,
                                             final String ccdDocumentType,
                                             final String template,
                                             final String fileName) throws WorkflowException {
        return this.execute(
            new Task[] {setFormattedDnCourtDetails, documentGenerationTask, addNewDocumentsToCaseDataTask},
            caseDetails.getCaseData(),
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(DOCUMENT_TYPE, ccdDocumentType),
            ImmutablePair.of(DOCUMENT_TEMPLATE_ID, template),
            ImmutablePair.of(DOCUMENT_FILENAME, fileName)
        );
    }

}