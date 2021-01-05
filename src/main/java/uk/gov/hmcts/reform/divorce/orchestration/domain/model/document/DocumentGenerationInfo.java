package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentTypeHelper;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentGenerationRequest;

import java.util.Map;

@EqualsAndHashCode
@ToString
@RequiredArgsConstructor
public class DocumentGenerationInfo {

    private final DocumentType documentType;
    private final String documentTypeForm;
    private final String documentFileName;

    public DocumentGenerationRequest buildDocumentGenerationRequest(Map<String, Object> caseData) {
        String templateId = DocumentTypeHelper.getLanguageAppropriateTemplate(caseData, documentType);
        return new DocumentGenerationRequest(templateId, documentTypeForm, documentFileName);
    }

}