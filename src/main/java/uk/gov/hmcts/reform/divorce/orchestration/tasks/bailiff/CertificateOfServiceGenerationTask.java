package uk.gov.hmcts.reform.divorce.orchestration.tasks.bailiff;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.documentupdate.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.CertificateOfService;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker;
import uk.gov.hmcts.reform.divorce.orchestration.util.mapper.CcdMappers;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CERTIFICATE_OF_SERVICE_DOCUMENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.CERTIFICATE_OF_SERVICE;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CaseDataUtils.formatCaseIdToReferenceNumber;

@Slf4j
@Component
public class CertificateOfServiceGenerationTask extends BasePayloadSpecificDocumentGenerationTask {

    public CertificateOfServiceGenerationTask(CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
                                              PdfDocumentGenerationService pdfDocumentGenerationService,
                                              CcdUtil ccdUtil) {
        super(ctscContactDetailsDataProviderService, pdfDocumentGenerationService, ccdUtil);
    }

    @Override
    protected DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) {
        return CertificateOfService.certificateOfServiceBuilder()
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .caseReference(formatCaseIdToReferenceNumber(context.getTransientObject(CASE_ID_JSON_KEY)))
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .hasCoRespondent(PartyRepresentationChecker.isCoRespondentLinkedToCase(caseData))
            .coRespondentFullName(FullNamesDataExtractor.getCoRespondentFullName(caseData))
            .build();
    }

    @Override
    protected Map<String, Object> addToCaseData(TaskContext context, Map<String, Object> caseData, GeneratedDocumentInfo documentInfo) {
        log.info("CaseID: {} Adding certificate of service to field {}.", getCaseId(context), CERTIFICATE_OF_SERVICE_DOCUMENT);
        caseData.put(CERTIFICATE_OF_SERVICE_DOCUMENT, CcdMappers.mapDocumentInfoToCcdDocument(documentInfo).getValue());
        return caseData;
    }

    @Override
    public String getTemplateId() {
        return CERTIFICATE_OF_SERVICE.getTemplateByLanguage(ENGLISH);
    }

    @Override
    public String getDocumentType() {
        return CERTIFICATE_OF_SERVICE.getTemplateLogicalName();
    }
}
