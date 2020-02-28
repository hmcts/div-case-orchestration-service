package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.DocumentGeneratorClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.DocumentType;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GenerateDocumentRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.fees.FeeResponse;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.DocumentTemplateService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Collections.singletonMap;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AMEND_PETITION_FEE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D8DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_CLARIFICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_DOCUMENT_NAME_OLD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_REFUSAL_REJECTION_DOCUMENT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSAL_DRAFT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_REFUSED_REJECT_OPTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_DRAFT_LINK_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_EXTENSION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME_FMT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_FILENAME_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_LINK_FILENAME_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_LINK_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_OTHER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.FEE_TO_PAY_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.REFUSAL_DECISION_MORE_INFO_VALUE;

@Component
@AllArgsConstructor
public class DecreeNisiRefusalDocumentGeneratorTask implements Task<Map<String, Object>> {

    private static final String VALUE_KEY = "value";

    private final DocumentGeneratorClient documentGeneratorClient;
    private final DocumentTemplateService documentTemplateService;
    private final CcdUtil ccdUtil;

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> caseData) {
        CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

        final Set<GeneratedDocumentInfo> documentCollection = context.computeTransientObjectIfAbsent(DOCUMENT_COLLECTION,
            new LinkedHashSet<>());

        // Rename and set previous refusal order documents to other type so it won't get overwritten
        List<Map<String, Object>> d8DocumentsGenerated =
            Optional.ofNullable((List<Map<String, Object>>) caseData.get(D8DOCUMENTS_GENERATED))
                .orElse(new ArrayList<>());

        d8DocumentsGenerated.stream().filter(collectionMember -> {
            Map<String, Object> document = (Map<String, Object>) collectionMember.get(VALUE_KEY);
            return DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE.equals(document.get(DOCUMENT_TYPE_JSON_KEY));
        }).forEach(collectionMember -> {
            String newFileName = format(DOCUMENT_FILENAME_FMT, DECREE_NISI_REFUSAL_DOCUMENT_NAME_OLD,
                ccdUtil.getCurrentDateCcdFormat());

            Map<String, Object> document = (Map<String, Object>) collectionMember.get(VALUE_KEY);
            document.put(DOCUMENT_TYPE_JSON_KEY, DOCUMENT_TYPE_OTHER);
            document.put(DOCUMENT_FILENAME_JSON_KEY,newFileName);

            Map<String, Object> documentLink = (Map<String, Object>) document.get(DOCUMENT_LINK_JSON_KEY);
            documentLink.put(DOCUMENT_LINK_FILENAME_JSON_KEY, newFileName.concat(DOCUMENT_EXTENSION));
        });

        if (REFUSAL_DECISION_MORE_INFO_VALUE.equalsIgnoreCase((String) caseData.get(REFUSAL_DECISION_CCD_FIELD))) {
            String templateId = getTemplateId(documentTemplateService,
                    DocumentType.DECREE_NISI_REFUSAL_ORDER_CLARIFICATION_TEMPLATE_ID,
                    caseData);

            GeneratedDocumentInfo generatedDocumentInfo = generatePdfDocument(
                    templateId,
                DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE,
                DECREE_NISI_REFUSAL_CLARIFICATION_DOCUMENT_NAME,
                context.getTransientObject(AUTH_TOKEN_JSON_KEY),
                caseDetails.toBuilder().caseData(caseData).build()
            );

            documentCollection.add(generatedDocumentInfo);
            setDraftLinkInContext(context, DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE, DN_REFUSAL_DRAFT);
        } else if (DN_REFUSED_REJECT_OPTION.equalsIgnoreCase((String) caseData.get(REFUSAL_DECISION_CCD_FIELD))) {
            FeeResponse amendFee = context.getTransientObject(AMEND_PETITION_FEE_JSON_KEY);

            // Remove reference to existing caseDetails so the context case details is not updated
            Map<String, Object> caseDataToSend = new HashMap<>(caseData);
            caseDataToSend.put(FEE_TO_PAY_JSON_KEY, amendFee.getFormattedFeeAmount());

            String templateId = getTemplateId(documentTemplateService,
                    DocumentType.DECREE_NISI_REFUSAL_ORDER_REJECTION_TEMPLATE_ID,
                    caseData);

            GeneratedDocumentInfo generatedDocumentInfo = generatePdfDocument(
                    templateId,
                DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE,
                DECREE_NISI_REFUSAL_REJECTION_DOCUMENT_NAME,
                context.getTransientObject(AUTH_TOKEN_JSON_KEY),
                caseDetails.toBuilder().caseData(caseDataToSend).build()
            );

            documentCollection.add(generatedDocumentInfo);
            setDraftLinkInContext(context, DECREE_NISI_REFUSAL_ORDER_DOCUMENT_TYPE, DN_REFUSAL_DRAFT);
        }

        return caseData;
    }

    private void setDraftLinkInContext(final TaskContext context, String documentType, String docLinkFieldName) {
        context.setTransientObject(DOCUMENT_DRAFT_LINK_FIELD, docLinkFieldName);
        context.setTransientObject(DOCUMENT_TYPE, documentType);
    }

    private GeneratedDocumentInfo generatePdfDocument(String templateId, String documentType, String documentName,
                                                      String authToken, CaseDetails caseDetails) {
        final GeneratedDocumentInfo generatedDocumentInfo =
            documentGeneratorClient.generatePDF(
                GenerateDocumentRequest.builder()
                    .template(templateId)
                    .values(singletonMap(DOCUMENT_CASE_DETAILS_JSON_KEY, caseDetails))
                    .build(),
                authToken
            );

        generatedDocumentInfo.setDocumentType(documentType);
        generatedDocumentInfo.setFileName(format(DOCUMENT_FILENAME_FMT, documentName, caseDetails.getCaseId()));

        return generatedDocumentInfo;
    }
}
