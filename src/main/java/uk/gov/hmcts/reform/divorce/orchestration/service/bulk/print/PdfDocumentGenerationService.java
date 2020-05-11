package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.RequestTemplateVarsWrapper;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;

import java.util.Map;

import static java.util.Collections.singletonMap;

@Component
@AllArgsConstructor
public class PdfDocumentGenerationService {

    private final DocumentGeneratorClient documentGeneratorClient;

    public GeneratedDocumentInfo generatePdf(DocmosisTemplateVars templateModel, String templateId, String authorisationToken) {
        return documentGeneratorClient.generatePDF(
            GenerateDocumentRequest.builder()
                .template(templateId)
                .values(getPreparedDataFromContext(templateModel))
                .build(),
            authorisationToken
        );
    }

    private Map<String, Object> getPreparedDataFromContext(DocmosisTemplateVars model) {
        return singletonMap(
            OrchestrationConstants.CASE_DETAILS_JSON_KEY,
            new RequestTemplateVarsWrapper(model.getCaseReference(), model)
        );
    }
}
