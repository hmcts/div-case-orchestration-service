package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.Document;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.WorkflowException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.CourtOrderDocumentsUpdateService;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.DocumentGenerationWorkflow;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CERTIFICATE_OF_ENTITLEMENT_FILENAME_PREFIX;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.COSTS_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_ABSOLUTE_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_COE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.COSTS_ORDER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_ABSOLUTE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DECREE_NISI;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil.getCollectionMembersOrEmptyList;

@RequiredArgsConstructor
@Service
public class CourtOrderDocumentsUpdateServiceImpl implements CourtOrderDocumentsUpdateService {

    private final ObjectMapper objectMapper;
    private final DocumentGenerationWorkflow documentGenerationWorkflow;

    @Override
    public Map<String, Object> updateExistingCourtOrderDocuments(String authToken, CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        List<CollectionMember<Document>> generatedDocuments =
            getCollectionMembersOrEmptyList(objectMapper, caseDetails.getCaseData(), D8DOCUMENTS_GENERATED);

        if (isDocumentPresent(generatedDocuments, DOCUMENT_TYPE_COE)) {
            updateCertificateOfEntitlement(authToken, caseDetails);
        }

        if (isDocumentPresent(generatedDocuments, COSTS_ORDER_DOCUMENT_TYPE)) {
            updateCostsOrder(authToken, caseDetails);
        }

        if (isDocumentPresent(generatedDocuments, DECREE_NISI_DOCUMENT_TYPE)) {
            updateDecreeNisi(authToken, caseDetails);
        }

        if (isDocumentPresent(generatedDocuments, DECREE_ABSOLUTE_DOCUMENT_TYPE)) {
            updateDecreeAbsolute(authToken, caseDetails);
        }

        return caseDetails.getCaseData();
    }

    private boolean isDocumentPresent(List<CollectionMember<Document>> generatedDocuments, String ccdDocumentType) {
        return generatedDocuments.stream()
            .map(CollectionMember::getValue)
            .map(Document::getDocumentType)
            .anyMatch(ccdDocumentType::equals);
    }

    private void updateCertificateOfEntitlement(String authToken, CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        generateDocument(authToken, caseDetails, DOCUMENT_TYPE_COE, COE, CERTIFICATE_OF_ENTITLEMENT_FILENAME_PREFIX);
    }

    private void updateCostsOrder(String authToken, CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        generateDocument(authToken, caseDetails, COSTS_ORDER_DOCUMENT_TYPE, COSTS_ORDER, COSTS_ORDER_DOCUMENT_TYPE);
    }

    private void updateDecreeNisi(String authToken, CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        generateDocument(authToken, caseDetails, DECREE_NISI_DOCUMENT_TYPE, DECREE_NISI, DECREE_NISI_FILENAME);
    }

    private void updateDecreeAbsolute(String authToken, CaseDetails caseDetails) throws CaseOrchestrationServiceException {
        generateDocument(authToken, caseDetails, DECREE_ABSOLUTE_DOCUMENT_TYPE, DECREE_ABSOLUTE, DECREE_ABSOLUTE_FILENAME);
    }

    private void generateDocument(String authToken,
                                  CaseDetails caseDetails,
                                  String documentTypeCoe,
                                  DocumentType coe,
                                  String certificateOfEntitlementFilenamePrefix) throws CaseOrchestrationServiceException {
        String caseId = caseDetails.getCaseId();

        try {
            Map<String, Object> returnedCaseData =
                documentGenerationWorkflow.run(caseDetails, authToken, documentTypeCoe, coe, certificateOfEntitlementFilenamePrefix);
            caseDetails.setCaseData(returnedCaseData);
        } catch (WorkflowException exception) {
            throw new CaseOrchestrationServiceException(exception, caseId);
        }
    }

}