package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DivorceGeneralOrder;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.GeneralOrderParty;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.GeneralOrder;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.docmosis.DocmosisTemplateVars;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.exception.JudgeTypeNotFoundException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.InvalidDataForTaskException;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.FullNamesDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.GeneralOrderDataExtractor;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.JudgeTypesLookupService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing.BasePayloadSpecificDocumentGenerationTask;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker;
import uk.gov.hmcts.reform.divorce.orchestration.util.mapper.CcdMappers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDERS;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
public class GeneralOrderGenerationTask extends BasePayloadSpecificDocumentGenerationTask {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FileMetadata {
        public static final String TEMPLATE_ID = "FL-DIV-GOR-ENG-00572.docx";
        public static final String DOCUMENT_TYPE = "generalOrder";
    }

    private final JudgeTypesLookupService judgeTypesLookupService;

    public GeneralOrderGenerationTask(
        CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
        PdfDocumentGenerationService pdfDocumentGenerationService,
        CcdUtil ccdUtil,
        JudgeTypesLookupService judgeTypesLookupService) {
        super(ctscContactDetailsDataProviderService, pdfDocumentGenerationService, ccdUtil);
        this.judgeTypesLookupService = judgeTypesLookupService;
    }

    @Override
    protected Map<String, Object> addToCaseData(
        TaskContext context, Map<String, Object> caseData, GeneratedDocumentInfo generatedDocumentInfo
    ) {
        log.info("CaseID: {} Adding general order to {} collection.", getCaseId(context), GENERAL_ORDERS);

        CollectionMember<DivorceGeneralOrder> collectionMember = getDivorceGeneralOrderCollectionMember(
            GeneralOrderDataExtractor.getGeneralOrderParties(caseData), generatedDocumentInfo
        );

        return addNewElementToCollection(caseData, collectionMember);
    }

    @Override
    protected GeneratedDocumentInfo populateMetadataForGeneratedDocument(GeneratedDocumentInfo documentInfo) {
        documentInfo.setDocumentType(getDocumentType());
        documentInfo.setFileName(getFilenameWithCurrentDate());
        return documentInfo;
    }

    @Override
    protected DocmosisTemplateVars prepareDataForPdf(TaskContext context, Map<String, Object> caseData) {
        return GeneralOrder.generalOrderBuilder()
            .caseReference(getCaseId(context))
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .petitionerFullName(FullNamesDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(FullNamesDataExtractor.getRespondentFullName(caseData))
            .hasCoRespondent(PartyRepresentationChecker.isCoRespondentLinkedToCase(caseData))
            .coRespondentFullName(FullNamesDataExtractor.getCoRespondentFullName(caseData))
            .judgeName(GeneralOrderDataExtractor.getJudgeName(caseData))
            .judgeType(getJudgeType(caseData))
            .generalOrderRecitals(GeneralOrderDataExtractor.getGeneralOrderRecitals(caseData))
            .generalOrderDetails(GeneralOrderDataExtractor.getGeneralOrderDetails(caseData))
            .generalOrderDate(GeneralOrderDataExtractor.getGeneralOrderDate(caseData))
            .build();
    }

    @Override
    public String getTemplateId() {
        return FileMetadata.TEMPLATE_ID;
    }

    @Override
    public String getDocumentType() {
        return FileMetadata.DOCUMENT_TYPE;
    }

    private String getJudgeType(Map<String, Object> caseData) {
        try {
            return judgeTypesLookupService.getJudgeTypeByCode(GeneralOrderDataExtractor.getJudgeType(caseData));
        } catch (JudgeTypeNotFoundException e) {
            throw new InvalidDataForTaskException(e);
        }
    }

    private Map<String, Object> addNewElementToCollection(
        Map<String, Object> caseData, CollectionMember<DivorceGeneralOrder> collectionMember
    ) {
        Map<String, Object> copiedCaseData = new HashMap<>(caseData);

        List<CollectionMember<DivorceGeneralOrder>> allGeneralOrders = ccdUtil.getListOfCollectionMembers(
            copiedCaseData, GENERAL_ORDERS
        );

        allGeneralOrders.add(collectionMember);
        copiedCaseData.put(GENERAL_ORDERS, allGeneralOrders);

        return copiedCaseData;
    }

    private CollectionMember<DivorceGeneralOrder> getDivorceGeneralOrderCollectionMember(
        List<GeneralOrderParty> caseData, GeneratedDocumentInfo generatedDocumentInfo
    ) {
        DivorceGeneralOrder divorceGeneralOrder = DivorceGeneralOrder.builder()
            .generalOrderParties(caseData)
            .document(CcdMappers.mapDocumentInfoToCcdDocument(generatedDocumentInfo).getValue())
            .build();

        CollectionMember<DivorceGeneralOrder> collectionMember = new CollectionMember<>();
        collectionMember.setValue(divorceGeneralOrder);

        return collectionMember;
    }
}
