package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationTest.NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationTest.ORG_ID;

public class OrganisationPolicyTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void isPopulatedReturnsTrue() {
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID("TEST_ID").build()
        ).build();

        assertThat(organisationPolicy.isPopulated(), is(true));
    }

    @Test
    public void isPopulatedReturnsFalse() {
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().build()
        ).build();

        assertThat(organisationPolicy.isPopulated(), is(false));
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
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(organisation).build();
        String json = mapper.writeValueAsString(organisationPolicy);

        assertThat(json, not(containsString("populated")));
        assertThat(json, not(containsString("isPopulated")));
    }
}
