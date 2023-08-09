package uk.gov.hmcts.reform.divorce.orchestration.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.DnCourt;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DnCourtDetailsConfigValidationTest {

    // Maintain list separately so if property is accidentally removed, this test will fail
    private final List<String> knownCourtIds = Arrays.asList(
        "birmingham",
        "bradford",
        "liverpool",
        "newport",
        "nottingham",
        "nottinghamJustice",
        "portTalbot",
        "southampton",
        "wrexham"
    );

    @Autowired
    private DnCourtDetailsConfig dnCourtDetailsConfig;

    @Test
    public void validateListOfDnCourts() {
        knownCourtIds.stream().forEach(courtId -> {
            DnCourt dnCourt = dnCourtDetailsConfig.getLocations().get(courtId);
            assertThat(dnCourt.getName(), notNullValue());
            assertThat(dnCourt.getAddress(), notNullValue());
            assertThat(dnCourt.getEmail(), notNullValue());
            assertThat(dnCourt.getPhone(), notNullValue());
            assertThat(dnCourt.getFormattedContactDetails(), notNullValue());
        });
    }
}
