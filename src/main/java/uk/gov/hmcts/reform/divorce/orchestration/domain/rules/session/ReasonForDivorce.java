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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Rule(order = 14)
@Data
public class ReasonForDivorce {

    private static final String BLANK_SPACE = " ";
    private static final String ACTUAL_DATA = "Actual data is: %s";
    private static final String ERROR_MESSAGE_NULL = "reasonForDivorce can not be null or empty.";
    private static final String ERROR_MESSAGE_INVALID = "reasonForDivorce is invalid for the current date of marriage.";

    @Result
    public List<String> result;

    @Given("divorceSession")
    public DivorceSession divorceSession = new DivorceSession();

    @When
    public boolean when() {
        return !Optional.ofNullable(divorceSession.getReasonForDivorce()).isPresent()
            || getAllowedReasonsForDivorce(divorceSession.getMarriageDate()).stream()
                .noneMatch(reason -> reason.equalsIgnoreCase(divorceSession.getReasonForDivorce()));
    }

    @Then
    public void then() {
        result.add(String.join(
            BLANK_SPACE, // delimiter
            Optional.ofNullable(divorceSession.getReasonForDivorce()).isPresent()
                ? ERROR_MESSAGE_INVALID
                : ERROR_MESSAGE_NULL,
            String.format(ACTUAL_DATA, divorceSession.getReasonForDivorce())
        ));
    }

    private List<String> getAllowedReasonsForDivorce(Date marriageDate) {
        // Exit early if marriageDate is null
        if (marriageDate == null) {
            return new ArrayList<>();
        }

        List<String> reasonsForDivorce = new ArrayList<>();

        // When marriageDate is more than one year ago
        if (marriageDate.toInstant().isBefore(Instant.now().minus(365, ChronoUnit.DAYS))) {
            reasonsForDivorce.add("adultery");
            reasonsForDivorce.add("unreasonable-behaviour");
        }

        // When marriageDate is more than two years ago
        if (marriageDate.toInstant().isBefore(Instant.now().minus(365 * 2, ChronoUnit.DAYS))) {
            reasonsForDivorce.add("separation-2-years");
            reasonsForDivorce.add("desertion");
        }

        // When marriageDate is more than five years ago
        if (marriageDate.toInstant().isBefore(Instant.now().minus(365 * 5, ChronoUnit.DAYS))) {
            reasonsForDivorce.add("separation-5-years");
        }

        return reasonsForDivorce;
    }


}