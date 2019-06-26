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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_CO_RESPONDENT_INVITATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DOCUMENT_TYPE_PETITION;

@Slf4j
@Component
public class CoRespondentAosPackPrinter implements Task<Map<String, Object>> {

    private static final String LETTER_TYPE_CO_RESPONDENT_PACK = "co-respondent-aos-pack";

    private final BulkPrintService bulkPrintService;

    @Autowired
    public CoRespondentAosPackPrinter(final BulkPrintService bulkPrintService) {
        this.bulkPrintService = bulkPrintService;
    }

    @Override
    public Map<String, Object> execute(final TaskContext context, final Map<String, Object> payload) {

        final CaseDetails caseDetails = context.getTransientObject(CASE_DETAILS_JSON_KEY);

        final Map<String, GeneratedDocumentInfo> generatedDocumentInfoList = context.getTransientObject(DOCUMENTS_GENERATED);

        final GeneratedDocumentInfo miniPetition = generatedDocumentInfoList.get(DOCUMENT_TYPE_PETITION);
        final GeneratedDocumentInfo coRespondentLetter = generatedDocumentInfoList.get(DOCUMENT_TYPE_CO_RESPONDENT_INVITATION);

        // The order of co-respondent Letter and miniPetition arguments is important here.
        // Sending the co-respondent letter first ensures it is the first piece of paper in the envelope so that the address label is displayed.

        if (coRespondentLetter != null && miniPetition != null) {
            try {
                bulkPrintService.send(caseDetails.getCaseId(), LETTER_TYPE_CO_RESPONDENT_PACK, asList(coRespondentLetter, miniPetition));
            } catch (final Exception e) {
                context.setTaskFailed(true);
                log.error(String.format("Co-respondent pack bulk print failed for case [%s]", caseDetails.getCaseId()), e);
                context.setTransientObject(BULK_PRINT_ERROR_KEY, "Bulk print failed for co-respondent pack");
            }
        }

        return payload;
    }
}
