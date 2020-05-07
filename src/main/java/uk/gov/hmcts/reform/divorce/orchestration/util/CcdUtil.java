package uk.gov.hmcts.reform.divorce.orchestration.util;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.utils.DateUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import static java.time.format.DateTimeFormatter.ofPattern;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_DATE_FORMAT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.PAYMENT_DATE_PATTERN;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;

@SuppressWarnings("squid:S1118")
@AllArgsConstructor
@Component
public class CcdUtil {
    private static final String UK_HUMAN_READABLE_DATE_FORMAT = "dd/MM/yyyy";

    private final Clock clock;
    @Autowired
    private LocalDateToWelshStringConverter localDateToWelshStringConverter;


    public String getCurrentDateCcdFormat() {
        return LocalDate.now(clock).format(DateTimeFormatter.ofPattern(CCD_DATE_FORMAT));
    }

    public String getCurrentDatePaymentFormat() {
        return LocalDate.now(clock).format(DateTimeFormatter.ofPattern(PAYMENT_DATE_PATTERN));
    }

    public String mapCCDDateToDivorceDate(String date) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(CCD_DATE_FORMAT))
            .format(DateTimeFormatter.ofPattern(PAYMENT_DATE_PATTERN));
    }

    public static String mapDivorceDateTimeToCCDDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"));
    }

    public static LocalDateTime mapCCDDateTimeToLocalDateTime(String dateTime) {
        return LocalDateTime.parse(dateTime);
    }

    public String getCurrentDateWithCustomerFacingFormat() {
        return DateUtils.formatDateWithCustomerFacingFormat(java.time.LocalDate.now(clock));
    }

    public String getFormattedDueDate(Map<String, Object> caseData, String dateToFormat) throws TaskException {
        LocalDate dueDate = getLocalDate(caseData, dateToFormat);
        return DateUtils.formatDateWithCustomerFacingFormat(dueDate);
    }

    public String getWelshFormattedDate(Map<String, Object> caseData, String dateToFormat) throws TaskException {
        LocalDate localDate = getLocalDate(caseData, dateToFormat);
        return localDateToWelshStringConverter.convert(localDate);
    }

    private LocalDate getLocalDate(Map<String, Object> caseData, String dateToFormat) throws TaskException {
        String dateAsString = getMandatoryPropertyValueAsString(caseData, dateToFormat);
        return LocalDate.parse(dateAsString);
    }

    public boolean isCcdDateTimeInThePast(String date) {
        return LocalDateTime.parse(date).toLocalDate().isBefore(LocalDate.now(clock).plusDays(1));
    }

    public String parseDecreeAbsoluteEligibleDate(LocalDate grantedDate) {
        return DateUtils.formatDateFromLocalDate(
            grantedDate.plusWeeks(6).plusDays(1)
        );
    }

    public static LocalDate parseDateUsingCcdFormat(String date) {
        return LocalDate.parse(date, ofPattern(CCD_DATE_FORMAT));
    }

    public static String formatDateForCCD(LocalDate plus) {
        return plus.format(ofPattern(CCD_DATE_FORMAT));
    }

    public static String formatFromCCDFormatToHumanReadableFormat(String inputDate) {
        LocalDate localDate = parseDateUsingCcdFormat(inputDate);
        return localDate.format(DateTimeFormatter.ofPattern(UK_HUMAN_READABLE_DATE_FORMAT));
    }

    public static String retrieveAndFormatCCDDateFieldIfPresent(String fieldName, Map<String, Object> caseData, String defaultValue) {
        return Optional.ofNullable(caseData.get(fieldName))
            .map((String.class::cast))
            .map(CcdUtil::formatFromCCDFormatToHumanReadableFormat)
            .orElse(defaultValue);
    }

    public LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now(clock);
    }

    public String getEventIdForWelshCase(String currentEvent, Supplier<String> welshEventId,
                                         CaseDetails currentCaseDetails) {
        return Optional.ofNullable(currentCaseDetails).map(caseDetails -> CaseDataUtils.getLanguagePreference(caseDetails.getCaseData()))
                .filter(Objects::nonNull)
                .flatMap(value -> value).filter(value -> LanguagePreference.WELSH.equals(value))
                .map(value -> welshEventId.get()).orElse(currentEvent);
    }

}