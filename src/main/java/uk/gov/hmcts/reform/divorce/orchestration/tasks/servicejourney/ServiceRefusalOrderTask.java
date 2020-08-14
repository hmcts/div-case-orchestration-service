package uk.gov.hmcts.reform.divorce.orchestration.tasks.servicejourney;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ApplicationServiceTypes;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.ServiceApplicationRefusalOrder;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DatesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.mapper.CcdMappers;
import uk.gov.hmcts.reform.divorce.orchestration.workflows.servicejourney.ServiceRefusalOrderWorkflow;

import java.util.Map;

import static java.util.Collections.singletonList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVICE_REFUSAL_DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CaseDataExtractor.getServiceApplicationGranted;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
public class ServiceRefusalOrderTask implements Task<Map<String, Object>> {

    public static final String FINAL_DECISION = "final";
    public static final String DRAFT_DECISION = "draft";

    protected final CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService;
    private final PdfDocumentGenerationService pdfDocumentGenerationService;
    private final CcdUtil ccdUtil;

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FileMetadata {
        public static final String DEEMED_TEMPLATE_ID = "FL-DIV-GNO-ENG-00533.docx";
        public static final String DEEMED_DOCUMENT_TYPE = "DeemedServiceRefused";
        public static final String DISPENSE_TEMPLATE_ID = "FL-DIV-GNO-ENG-00535.docx";
        public static final String DISPENSE_DOCUMENT_TYPE = "DispenseWithServiceRefused";
    }

    public ServiceRefusalOrderTask(
        CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
        PdfDocumentGenerationService pdfDocumentGenerationService,
        CcdUtil ccdUtil) {
        this.ctscContactDetailsDataProviderService = ctscContactDetailsDataProviderService;
        this.pdfDocumentGenerationService = pdfDocumentGenerationService;
        this.ccdUtil = ccdUtil;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {

        if (YES_VALUE.equals(getServiceApplicationGranted(caseData))) {
            log.info("CaseID: {} - Case must be awaiting service consideration and service application granted to proceed.", getCaseId(context));

            return caseData;
        }

        String serviceDecision = getDecision(context);
        GeneratedDocumentInfo generatedDocumentInfo = generateServiceRefusalOrder(context, caseData);

        if (FINAL_DECISION.equals(serviceDecision)) {
            ccdUtil.addNewDocumentsToCaseData(caseData, singletonList(generatedDocumentInfo));
            caseData.remove(SERVICE_REFUSAL_DRAFT);
            log.info("CaseID: {} - Draft Service Refusal Order Document removed from case data.", getCaseId(context));

        } else if (DRAFT_DECISION.equals(serviceDecision)) {
            caseData.put(SERVICE_REFUSAL_DRAFT, generateRefusalDraftDocumentLink(generatedDocumentInfo));
            log.info("CaseID: {} - Added Draft Service Refusal Order Document.", getCaseId(context));
        }

        return caseData;
    }

    String getRefusalOrderTemplateId(Map<String, Object> caseData) {
        String serviceType = CaseDataExtractor.getServiceApplicationType(caseData);
        return ApplicationServiceTypes.DEEMED.equals(serviceType) ?
            FileMetadata.DEEMED_TEMPLATE_ID : FileMetadata.DISPENSE_TEMPLATE_ID;
    }

    private String getDecision(TaskContext context) {
        return context.getTransientObject(ServiceRefusalOrderWorkflow.SERVICE_DECISION);
    }

    private GeneratedDocumentInfo generateServiceRefusalOrder(TaskContext context, Map<String, Object> caseData) {
        log.info("CaseID {}: Generating document service refusal document", getCaseId(context));
        return pdfDocumentGenerationService.generatePdf(
            getServiceDecisionRefusalOrderTemplate(caseData),
            getRefusalOrderTemplateId(caseData),
            context.getTransientObject(AUTH_TOKEN_JSON_KEY)
        );
    }

    private ServiceApplicationRefusalOrder getServiceDecisionRefusalOrderTemplate(Map<String, Object> caseData) {
        return ServiceApplicationRefusalOrder.serviceApplicationRefusalOrderBuilder()
            .caseReference(getCaseReference(caseData))
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .receivedServiceApplicationDate(DatesDataExtractor.getReceivedServiceApplicationDate(caseData))
            .serviceApplicationRefusalReason(CaseDataExtractor.getServiceApplicationRefusalReason(caseData))
            .documentIssuedOn(DatesDataExtractor.getLetterDate())
            .build();
    }

    private DocumentLink generateRefusalDraftDocumentLink(GeneratedDocumentInfo generatedDocumentInfo) {
        return CcdMappers.mapDocumentInfoToCcdDocument(generatedDocumentInfo)
            .getValue()
            .getDocumentLink();
    }

    private String getCaseReference(Map<String, Object> caseData) {
        return (String)caseData.get(CASE_ID_JSON_KEY);
    }

}
