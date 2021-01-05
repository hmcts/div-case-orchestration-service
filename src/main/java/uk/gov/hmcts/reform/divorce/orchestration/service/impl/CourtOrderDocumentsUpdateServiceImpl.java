package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CourtOrderDocumentsUpdateService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DocumentGenerationWorkflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocmosisTemplates.COE_ENGLISH_TEMPLATE_ID;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.getCollectionMembersOrEmptyList;

@RequiredArgsConstructor
@Service
public class CourtOrderDocumentsUpdateServiceImpl implements CourtOrderDocumentsUpdateService {

    private static final String CERTIFICATE_OF_ENTITLEMENT_FILE_NAME = "certificateOfEntitlement";

    private final ObjectMapper objectMapper;
    private final DocumentGenerationWorkflow documentGenerationWorkflow;

    @Override
    public Map<String, Object> updateExistingCourtOrderDocuments(String authToken, CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        String caseId = caseDetails.getCaseId();
        Map<String, Object> caseData = new HashMap<>(caseDetails.getCaseData());

        List<CollectionMember<Document>> generatedDocuments = getCollectionMembersOrEmptyList(objectMapper, caseData, D8DOCUMENTS_GENERATED);

        boolean coeDocumentExists = generatedDocuments.stream()
            .map(CollectionMember::getValue)
            .map(Document::getDocumentType)
            .anyMatch(DOCUMENT_TYPE_COE::equals);

        if (coeDocumentExists) {
            updateCertificateOfEntitlement(authToken, caseDetails, caseId, caseData);
        }

        return caseData;
    }

    private void updateCertificateOfEntitlement(String authToken, CaseDetails caseDetails, String caseId, Map<String, Object> caseData)
        throws CaseOrchestrationServiceException {
        try {
            Map<String, Object> returnedPayload = documentGenerationWorkflow.run(caseDetails, authToken,
                COE_ENGLISH_TEMPLATE_ID, DOCUMENT_TYPE_COE, CERTIFICATE_OF_ENTITLEMENT_FILE_NAME);
            caseData.putAll(returnedPayload);
        } catch (WorkflowException exception) {
            throw new CaseOrchestrationServiceException(exception, caseId);
        }
    }

}