package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.AOSPackOfflineConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.DocumentGenerationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;

import java.util.Map;

@Setter
@EqualsAndHashCode
public class DocumentGenerator {//TODO - revisit this - why is this needed? - This is a bean - next PR

    private DocumentType documentType;
    private AOSPackOfflineConstants documentTypeForm;
    private AOSPackOfflineConstants documentFileName;

    //TODO - this and template could probably be the same class??? - next PR
    public DocumentGenerationRequest getDocumentGenerationRequest(DocumentTemplateService documentTemplateService, Map<String, Object> caseData) {
        String templateId = documentTemplateService.getTemplateId(caseData, documentType);//TODO - do I still need this? maybe in different way? - next PR
        return new DocumentGenerationRequest(templateId, documentTypeForm.getValue(), documentFileName.getValue());
    }

}