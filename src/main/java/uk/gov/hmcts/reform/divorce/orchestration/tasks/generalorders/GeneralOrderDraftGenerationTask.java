package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.DocumentLink;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.PdfDocumentGenerationService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.JudgeTypesLookupService;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.mapper.CcdMappers;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

@Component
@Slf4j
public class GeneralOrderDraftGenerationTask extends GeneralOrderGenerationTask {

    public GeneralOrderDraftGenerationTask(
        CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService,
        PdfDocumentGenerationService pdfDocumentGenerationService,
        CcdUtil ccdUtil,
        JudgeTypesLookupService judgeTypesLookupService) {
        super(ctscContactDetailsDataProviderService, pdfDocumentGenerationService, ccdUtil, judgeTypesLookupService);
    }

    @Override
    protected Map<String, Object> addToCaseData(
        TaskContext context, Map<String, Object> caseData, GeneratedDocumentInfo generatedDocumentInfo
    ) {
        log.info("CaseID: {} Adding General Order draft.", getCaseId(context));
        caseData.put(CcdFields.GENERAL_ORDER_DRAFT, toDocumentLink(generatedDocumentInfo));

        return caseData;
    }

    private DocumentLink toDocumentLink(GeneratedDocumentInfo documentInfo) {
        documentInfo.setFileName(nameWithCurrentDate());

        return CcdMappers.mapDocumentInfoToCcdDocument(documentInfo)
            .getValue()
            .getDocumentLink();
    }
}
