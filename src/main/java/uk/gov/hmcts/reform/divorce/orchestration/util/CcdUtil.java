package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import static java.time.format.DateTimeFormatter.ofPattern;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;

@SuppressWarnings("squid:S1118")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CcdUtil {

    public static String getCurrentDate() {
        return LocalDate.now().format(ofPattern(CCD_DATE_FORMAT));
    }
}
