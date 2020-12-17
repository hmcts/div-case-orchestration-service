package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CourtsOrderDocumentsUpdateService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DocumentGenerationWorkflow;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;

@RequiredArgsConstructor
public class CourtsOrderDocumentsUpdateServiceImpl implements CourtsOrderDocumentsUpdateService {

    private static final String COE_ENGLISH_TEMPLATE = "FL-DIV-GNO-ENG-00020.docx";//TODO - this is unnecessary - it's also ignored
    private static final String CERTIFICATE_OF_ENTITLEMENT_FILE_NAME = "certificateOfEntitlement";

    private final DocumentGenerationWorkflow documentGenerationWorkflow;

    @Override
    public void updateExistingCourtOrderDocuments(CaseDetails caseDetails, String authToken) {
        //COE
        try {
            documentGenerationWorkflow.run(caseDetails, authToken, COE_ENGLISH_TEMPLATE, DOCUMENT_TYPE_COE, CERTIFICATE_OF_ENTITLEMENT_FILE_NAME);
        } catch (WorkflowException exception) {
            exception.printStackTrace();//TODO - DWT
        }
    }

}