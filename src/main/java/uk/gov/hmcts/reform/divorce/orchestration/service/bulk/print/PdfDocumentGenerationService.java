package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.RequestTemplateVarsWrapper;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;

import java.util.Map;

import static java.util.Collections.singletonMap;

@Component
@AllArgsConstructor
public class PdfDocumentGenerationService {

    /*
     * Document Generator Service requires structure:
     * caseDetails: string id, map case_data
     */
    public static final String DGS_DATA_KEY = OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;

    private final DocumentGeneratorClient documentGeneratorClient;

    public GeneratedDocumentInfo generatePdf(DocmosisTemplateVars templateModel, String templateId, String authorisationToken) {
        return documentGeneratorClient.generatePDF(
            GenerateDocumentRequest.builder()
                .template(templateId)
                .values(wrapDocmosisTemplateVarsForDgs(templateModel))
                .build(),
            authorisationToken
        );
    }

    public static Map<String, Object> wrapDocmosisTemplateVarsForDgs(DocmosisTemplateVars model) {
        return singletonMap(
            DGS_DATA_KEY,
            new RequestTemplateVarsWrapper(model.getCaseReference(), model)
        );
    }
}
