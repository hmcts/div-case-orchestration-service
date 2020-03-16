package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.config.TemplateConfig;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalDateToWelshStringConverter {

    private final TemplateConfig templateConfig;

    public String convert(LocalDate dateToConvert) {
        if (dateToConvert == null) {
            return null;
        }
        int day = dateToConvert.getDayOfMonth();
        int year = dateToConvert.getYear();
        int month = dateToConvert.getMonth().getValue();
        return String.join(" ", Integer.toString(day),
                templateConfig.getTemplate().get(OrchestrationConstants.TEMPLATE_MONTHS).get(LanguagePreference.WELSH).get(String.valueOf(month)),
                Integer.toString(year));
    }
}
