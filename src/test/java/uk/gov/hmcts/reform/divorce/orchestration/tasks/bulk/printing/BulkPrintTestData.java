package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.documentgeneration.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.DefaultTaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.divorce.orchestration.TestConstants.AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_ID_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BulkPrintTestData {
    public static final String PETITIONERS_FIRST_NAME = "Anna";
    public static final String PETITIONERS_LAST_NAME = "Nowak";
    public static final String RESPONDENTS_FIRST_NAME = "John";
    public static final String RESPONDENTS_LAST_NAME = "Wozniak";

    public static final String CASE_ID = "It's mandatory field in context";
    public static final String LETTER_DATE_FROM_CCD = LocalDate.now().toString();
    public static final String LETTER_DATE_EXPECTED = formatDateWithCustomerFacingFormat(LocalDate.now());

    public static final CtscContactDetails CTSC_CONTACT = CtscContactDetails.builder().build();

    public static TaskContext prepareTaskContext() {
        TaskContext context = new DefaultTaskContext();
        context.setTransientObject(CASE_ID_JSON_KEY, CASE_ID);
        context.setTransientObject(AUTH_TOKEN_JSON_KEY, AUTH_TOKEN);

        return context;
    }

    public static GeneratedDocumentInfo createDocument() {
        return GeneratedDocumentInfo.builder()
            .fileName("myFile.pdf")
            .build();
    }
}
