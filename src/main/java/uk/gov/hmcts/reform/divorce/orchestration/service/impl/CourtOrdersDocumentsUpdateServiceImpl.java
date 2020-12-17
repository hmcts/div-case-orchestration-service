package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CourtOrdersDocumentsUpdateService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DocumentGenerationWorkflow;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;

@RequiredArgsConstructor
public class CourtOrdersDocumentsUpdateServiceImpl implements CourtOrdersDocumentsUpdateService {

    private static final String COE_ENGLISH_TEMPLATE = "FL-DIV-GNO-ENG-00020.docx";//TODO - this is unnecessary - it's also ignored
    private static final String CERTIFICATE_OF_ENTITLEMENT_FILE_NAME = "certificateOfEntitlement";

    private final DocumentGenerationWorkflow documentGenerationWorkflow;

    @Override
    public Map<String, Object> updateExistingCourtOrderDocuments(String authToken, CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseDataToReturn = new HashMap<>(caseDetails.getCaseData());

        //COE
        try {
            Map<String, Object> returnedPayload = documentGenerationWorkflow.run(caseDetails, authToken, COE_ENGLISH_TEMPLATE, DOCUMENT_TYPE_COE, CERTIFICATE_OF_ENTITLEMENT_FILE_NAME);
            caseDataToReturn.putAll(returnedPayload);
        } catch (WorkflowException exception) {
            throw new CaseOrchestrationServiceException(exception, caseId);
        }

        return caseDataToReturn;
    }

}