package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.CoERespondentSolicitorCoverLetter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.AddresseeDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CoECoverLetterDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.SolicitorDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.DN_GRANTED_COVER_LETTER_RESPONDENT_SOLICITOR;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
public class DnGrantedRespondentSolicitorCoverLetterGenerationTask extends BasePayloadSpecificDocumentGenerationTask {

    private final FeatureToggleService featureToggleService;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FileMetadata {
        public static final String TEMPLATE_ID = DN_GRANTED_COVER_LETTER_RESPONDENT_SOLICITOR.getTemplateByLanguage(ENGLISH);
        public static final String DOCUMENT_TYPE = "dnGrantedCoverLetterRespondentSolicitor";
    }

    public DnGrantedRespondentSolicitorCoverLetterGenerationTask(
        CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
        PdfDocumentGenerationService pdfDocumentGenerationService,
        CcdUtil ccdUtil,
        FeatureToggleService featureToggleService) {
        super(ctscContactDetailsDataProviderService, pdfDocumentGenerationService, ccdUtil);

        this.featureToggleService = featureToggleService;
    }

    @Override
    protected DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) {
        return CoERespondentSolicitorCoverLetter.coERespondentSolicitorCoverLetterBuilder()
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .addressee(AddresseeDataExtractor.getRespondentSolicitor(caseData))
            .caseReference(getCaseId(context))
            .letterDate(DatesDataExtractor.getLetterDate())
            .hearingDate(DatesDataExtractor.getHearingDate(caseData))
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .costClaimGranted(CoECoverLetterDataExtractor.isCostsClaimGranted(caseData, isObjectToCostsEnabled()))
            .solicitorReference(SolicitorDataExtractor.getSolicitorReference(caseData))
            .build();
    }

    @Override
    public String getDocumentType() {
        return FileMetadata.DOCUMENT_TYPE;
    }

    @Override
    public String getTemplateId() {
        return FileMetadata.TEMPLATE_ID;
    }

    private boolean isObjectToCostsEnabled() {
        return featureToggleService.isFeatureEnabled(Features.OBJECT_TO_COSTS);
    }
}
