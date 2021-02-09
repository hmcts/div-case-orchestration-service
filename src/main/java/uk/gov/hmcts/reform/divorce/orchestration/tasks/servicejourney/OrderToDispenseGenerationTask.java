package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DISPENSE_WITH_SERVICE_GRANTED;

@Component
public class OrderToDispenseGenerationTask extends ServiceDecisionOrderGenerationTask {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FileMetadata {
        public static final String TEMPLATE_ID = DISPENSE_WITH_SERVICE_GRANTED.getTemplateByLanguage(ENGLISH);
        public static final String DOCUMENT_TYPE = "dispenseWithServiceGranted";
    }

    public OrderToDispenseGenerationTask(
        CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
        PdfDocumentGenerationService pdfDocumentGenerationService,
        CcdUtil ccdUtil) {
        super(ctscContactDetailsDataProviderService, pdfDocumentGenerationService, ccdUtil);
    }

    @Override
    public String getTemplateId() {
        return FileMetadata.TEMPLATE_ID;
    }

    @Override
    public String getDocumentType() {
        return FileMetadata.DOCUMENT_TYPE;
    }
}
