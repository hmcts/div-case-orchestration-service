package uk.gov.hmcts.reform.divorce.orchestration.workflows;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.DefaultWorkflow;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.AddNewDocumentsToCaseDataTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.DocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.SetFormattedDnCourtDetails;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.getLanguagePreference;

@Slf4j
@Component
public class DocumentGenerationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SetFormattedDnCourtDetails setFormattedDnCourtDetails;

    private final DocumentGenerationTask documentGenerationTask;

    private final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    private final DocumentTemplateService documentTemplateService;

    @Autowired
    public DocumentGenerationWorkflow(final SetFormattedDnCourtDetails setFormattedDnCourtDetails,
                                      final DocumentGenerationTask documentGenerationTask,
                                      final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask,
                                      final DocumentTemplateService documentTemplateService) {
        this.setFormattedDnCourtDetails = setFormattedDnCourtDetails;
        this.documentGenerationTask = documentGenerationTask;
        this.addNewDocumentsToCaseDataTask = addNewDocumentsToCaseDataTask;
        this.documentTemplateService = documentTemplateService;
    }

    public Map<String, Object> run(final CaseDetails caseDetails,
                                   final String authToken,
                                   final String templateId,//TODO this is used in more than one place as well
                                   final String documentType, //TODO - why do I need both the templateId and the documentType? - maybe this should be decided before the workflow is called - it would be easier to reuse
                                   final String filename) throws WorkflowException {
        Map<String, Object> caseData = caseDetails.getCaseData();
        final String evalTemplateId = getTemplateId(templateId, documentType, caseData);
        log.debug("For language {}, evaluated template id {}", getLanguagePreference(caseData), evalTemplateId);

        return this.execute(
            new Task[] {setFormattedDnCourtDetails, documentGenerationTask, addNewDocumentsToCaseDataTask},
            caseData,
            ImmutablePair.of(AUTH_TOKEN_JSON_KEY, authToken),
            ImmutablePair.of(CASE_DETAILS_JSON_KEY, caseDetails),
            ImmutablePair.of(DOCUMENT_TYPE, documentType),
            ImmutablePair.of(DOCUMENT_TEMPLATE_ID, evalTemplateId),
            ImmutablePair.of(DOCUMENT_FILENAME, filename)
        );

    }

    private String getTemplateId(String templateId, String documentType, Map<String, Object> caseData) {
        Optional<DocumentType> optionalDocumentType = DocumentType.getEnum(documentType);

        if (optionalDocumentType.isPresent()) {
            try {
                return getConfiguredLanguageAppropriateTemplateId(documentTemplateService, optionalDocumentType.get(), caseData);
            } catch (IllegalArgumentException exception) {
                log.error("Missing template configuration in properties so returning as passed ", exception);//TODO - do we need this catch? reconsider this before getting rid of this code
            }
        }

        return templateId;
    }

}