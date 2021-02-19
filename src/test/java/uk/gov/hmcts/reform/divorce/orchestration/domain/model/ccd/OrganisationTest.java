package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class OrganisationTest {

    public static final String NAME = "name";
    public static final String ORG_ID = "id";

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void isPopulatedShouldReturnTrue() {
        Organisation organisation = Organisation.builder().organisationID("TEST_ID").build();
        assertThat(organisation.isPopulated(), is(true));
    }

    @Test
    public void isPopulatedShouldReturnFalse() {
        assertThat(Organisation.builder().build().isPopulated(), is(false));
        assertThat(Organisation.builder().organisationID(null).build().isPopulated(), is(false));
        assertThat(Organisation.builder().organisationID("").build().isPopulated(), is(false));
        assertThat(Organisation.builder().organisationName("populated").build().isPopulated(), is(false));
    }

    @Test
    public void jsonIgnoreForIsPopulatedShouldBlockMappingToJson() throws IOException {
        assertIsPopulatedIsNotMappedToJson(Organisation.builder().organisationID(ORG_ID).build());
        assertIsPopulatedIsNotMappedToJson(Organisation.builder().build());
        assertIsPopulatedIsNotMappedToJson(Organisation.builder().organisationName(NAME).build());
        assertIsPopulatedIsNotMappedToJson(
            Organisation.builder().organisationName(NAME).organisationID(ORG_ID).build()
        );
    }

    private void assertIsPopulatedIsNotMappedToJson(Organisation organisation)
        throws JsonProcessingException {
        String json = mapper.writeValueAsString(organisation);

        assertThat(json, not(containsString("populated")));
        assertThat(json, not(containsString("isPopulated")));
    }
}
