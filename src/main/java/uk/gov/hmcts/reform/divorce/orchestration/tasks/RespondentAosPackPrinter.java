package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.service.BulkPrintService;

import java.util.Map;

import static java.util.Arrays.asList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.BULK_PRINT_ERROR_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_DETAILS_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_RESPONDENT_INVITATION;

@Slf4j
@Component
public class RespondentAosPackPrinter implements Task<Map<String, Object>> {

    private static final String LETTER_TYPE_RESPONDENT_PACK = "respondent-aos-pack";

    private final BulkPrintService bulkPrintService;

    @Autowired
    public RespondentAosPackPrinter(final BulkPrintService bulkPrintService) {
        this.bulkPrintService = bulkPrintService;
    }

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> payload) {

        final CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

        final Map<String, GeneratedDocumentInfo> generatedDocumentInfoList = context.getTransientObject(DOCUMENTS_GENERATED);

        final GeneratedDocumentInfo miniPetition = generatedDocumentInfoList.get(DOCUMENT_TYPE_PETITION);
        final GeneratedDocumentInfo respondentAosLetter = generatedDocumentInfoList.get(DOCUMENT_TYPE_RESPONDENT_INVITATION);

        // The order of respondentAosLetter and miniPetition arguments is important here.
        // Sending the respondentAosLetter first ensures it is the first piece of paper in the envelope so that the address label is displayed.

        if (respondentAosLetter != null && miniPetition != null) {
            try {
                bulkPrintService.send(caseDetails.getCaseId(), LETTER_TYPE_RESPONDENT_PACK, asList(respondentAosLetter, miniPetition));
            } catch (final Exception e) {
                context.setTaskFailed(true);
                log.error("Respondent pack bulk print failed for case {}", caseDetails.getCaseId(), e);
                context.setTransientObject(BULK_PRINT_ERROR_KEY, "Bulk print failed for respondent pack");
            }
        }
        return payload;
    }
}
