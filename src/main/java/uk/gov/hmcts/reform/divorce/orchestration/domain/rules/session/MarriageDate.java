package uk.gov.hmcts.reform.divorce.orchestration.domain.rules.session;

import com.deliveredtechnologies.rulebook.annotation.Given;
import com.deliveredtechnologies.rulebook.annotation.Result;
import com.deliveredtechnologies.rulebook.annotation.Rule;
import com.deliveredtechnologies.rulebook.annotation.Then;
import com.deliveredtechnologies.rulebook.annotation.When;
import lombok.Data;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.validation.DivorceSession;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Rule(order = 2)
@Data
public class MarriageDate {

    private static final String BLANK_SPACE = " ";
    private static final String ACTUAL_DATA = "Actual data is: %s";
    private static final String ERROR_MESSAGE_NULL = "marriageDate can not be null or empty.";
    private static final String ERROR_MESSAGE_LESS_THAN_ONE_YEAR_AGO =
            "marriageDate can not be less than one year ago.";
    private static final String ERROR_MESSAGE_MORE_THAN_ONE_HUNDRED_YEARS_AGO =
            "marriageDate can not be more than 100 years ago.";
    private static final String ERROR_MESSAGE_IN_THE_FUTURE = "marriageDate can not be in the future.";

    @Result
    public List<String> result;

    @Given("divorceSession")
    public DivorceSession divorceSession = new DivorceSession();

    @When
    public boolean when() {
        Date marriageDate = divorceSession.getMarriageDate();
        return isNull()
                || isLessThanOneYearAgo(marriageDate)
                || isOverOneHundredYearsAgo(marriageDate)
                || isInTheFuture(marriageDate);
    }

    @Then
    public void then() {
        String errorMessage = deriveErrorMessage();

        result.add(String.join(
            BLANK_SPACE, // delimiter
            errorMessage,
            String.format(ACTUAL_DATA, divorceSession.getMarriageDate())
        ));
    }

    private boolean isNull() {
        return !Optional.ofNullable(divorceSession.getMarriageDate()).isPresent();
    }

    private boolean isLessThanOneYearAgo(Date date) {
        return !date.toInstant().isAfter(Instant.now())
            && date.toInstant().isAfter(Instant.now().minus(365, ChronoUnit.DAYS));
    }

    private boolean isOverOneHundredYearsAgo(Date date) {
        return date.toInstant().isBefore(Instant.now().minus(365 * 100, ChronoUnit.DAYS));
    }

    private boolean isInTheFuture(Date date) {
        return date.toInstant().isAfter(Instant.now());
    }

    private String deriveErrorMessage() {
        Date marriageDate = divorceSession.getMarriageDate();
        return isNull()
            ? ERROR_MESSAGE_NULL
            : Stream.of(
                isLessThanOneYearAgo(marriageDate) ? ERROR_MESSAGE_LESS_THAN_ONE_YEAR_AGO : "",
                isOverOneHundredYearsAgo(marriageDate) ? ERROR_MESSAGE_MORE_THAN_ONE_HUNDRED_YEARS_AGO : "",
                isInTheFuture(marriageDate) ? ERROR_MESSAGE_IN_THE_FUTURE : ""
            ).filter(string -> !string.isEmpty()).collect(Collectors.joining(BLANK_SPACE));
    }
}