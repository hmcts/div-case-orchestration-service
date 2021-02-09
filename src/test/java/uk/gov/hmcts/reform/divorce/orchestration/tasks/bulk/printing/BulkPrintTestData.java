package uk.gov.hmcts.reform.divorce.orchestration.tasks.bulk.printing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.bsp.common.model.document.CtscContactDetails;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateWithCustomerFacingFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BulkPrintTestData {

    public static final String SOLICITOR_REF = "SolRef1234";
    public static final String LETTER_DATE_FROM_CCD = LocalDate.now().toString();
    public static final String LETTER_DATE_EXPECTED = formatDateWithCustomerFacingFormat(LocalDate.now());

    public static final CtscContactDetails CTSC_CONTACT = CtscContactDetails.builder().build();
}
