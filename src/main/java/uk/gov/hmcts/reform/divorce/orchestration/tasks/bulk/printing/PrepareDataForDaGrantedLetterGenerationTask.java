package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.print.DaGrantedLetter;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.CtscContactDetailsDataProviderService;
import uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.dataextractor.DaGrantedLetterDataExtractor;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getCaseId;

/*
 * data for template: FL-FRM-APP-ENG-00009.docx
 */
@Component
public class PrepareDataForDaGrantedLetterGenerationTask extends PrepareDataForDocumentGenerationTask {

    public PrepareDataForDaGrantedLetterGenerationTask(CtscContactDetailsDataProviderService ctscContactDetailsDataProviderService) {
        super(ctscContactDetailsDataProviderService);
    }

    @Override
    public void addPreparedDataToContext(TaskContext context, Map<String, Object> caseData) throws TaskException {
        DaGrantedLetter daGrantedLetterData = DaGrantedLetter.builder()
            .caseReference(getCaseId(context))
            .ctscContactDetails(ctscContactDetailsDataProviderService.getCtscContactDetails())
            .addressee(DaGrantedLetterDataExtractor.getAddressee(caseData))
            .letterDate(DaGrantedLetterDataExtractor.getDaGrantedDate(caseData))
            .petitionerFullName(DaGrantedLetterDataExtractor.getPetitionerFullName(caseData))
            .respondentFullName(DaGrantedLetterDataExtractor.getRespondentFullName(caseData))
            .build();

        context.setTransientObject(
            PrepareDataForDocumentGenerationTask.ContextKeys.PREPARED_DATA_FOR_DOCUMENT_GENERATION,
            daGrantedLetterData
        );
    }
}
