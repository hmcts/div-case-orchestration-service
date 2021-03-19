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
import java.util.Objects;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_ENDCLAIM_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_COSTS_OPTIONS_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COSTS_ORDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_NISI;

@Slf4j
@Component
public class SingleCaseDocumentGenerationWorkflow extends DefaultWorkflow<Map<String, Object>> {

    private final SetFormattedDnCourtDetails setFormattedDnCourtDetails;

    private final DocumentGenerationTask documentGenerationTask;

    private final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask;

    @Autowired
    public SingleCaseDocumentGenerationWorkflow(final SetFormattedDnCourtDetails setFormattedDnCourtDetails,
                                                final DocumentGenerationTask documentGenerationTask,
                                                final AddNewDocumentsToCaseDataTask addNewDocumentsToCaseDataTask) {
        this.setFormattedDnCourtDetails = setFormattedDnCourtDetails;
        this.documentGenerationTask = documentGenerationTask;
        this.addNewDocumentsToCaseDataTask = addNewDocumentsToCaseDataTask;
    }


    public Map<String, Object> run(final CaseDetails caseDetails,
                                   final String authToken) throws WorkflowException {

        String template;

        if (isPetitionerClaimingCosts(caseDetails.getCaseData())) {
            template = getLanguageAppropriateTemplate(caseDetails, COSTS_ORDER);
            return executeTasks(caseDetails, authToken, COSTS_ORDER_DOCUMENT_TYPE, template, COSTS_ORDER_DOCUMENT_TYPE);
        }

        template = getLanguageAppropriateTemplate(caseDetails, DECREE_NISI);
        return executeTasks(caseDetails, authToken, DECREE_NISI_DOCUMENT_TYPE, template, DECREE_NISI_FILENAME);
    }

    private String getLanguageAppropriateTemplate(CaseDetails caseDetails, DocumentType documentType) {
        return DocumentTypeHelper.getLanguageAppropriateTemplate(caseDetails.getCaseData(), documentType);
    }

    public static boolean isPetitionerClaimingCosts(Map<String, Object> caseData) {
        return YES_VALUE.equalsIgnoreCase(String.valueOf(caseData.get(DIVORCE_COSTS_CLAIM_CCD_FIELD)))
            && !DN_COSTS_ENDCLAIM_VALUE.equalsIgnoreCase(String.valueOf(caseData.get(DN_COSTS_OPTIONS_CCD_FIELD)))
            && Objects.nonNull(caseData.get(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD));
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