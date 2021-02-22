package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Organisation;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.OrganisationPolicyHelper.isOrganisationPolicyPopulated;
import static uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper.OrganisationPolicyHelper.isOrganisationPopulated;

public class OrganisationPolicyHelperTest {

    @Test
    public void isOrganisationPolicyPopulatedReturnsTrue() {
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().organisationID("TEST_ID").build()
        ).build();

        assertThat(isOrganisationPolicyPopulated(organisationPolicy), is(true));
    }

    @Test
    public void isOrganisationPolicyPopulatedReturnsFalse() {
        OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().organisation(
            Organisation.builder().build()
        ).build();

        assertThat(isOrganisationPolicyPopulated(null), is(false));
        assertThat(isOrganisationPolicyPopulated(organisationPolicy), is(false));
    }

    @Test
    public void isOrganisationPopulatedShouldReturnTrue() {
        Organisation organisation = Organisation.builder().organisationID("TEST_ID").build();
        assertThat(isOrganisationPopulated(organisation), is(true));
    }

    @Test
    public void isOrganisationPopulatedShouldReturnFalse() {
        assertThat(isOrganisationPopulated(null), is(false));
        assertThat(isOrganisationPopulated(Organisation.builder().build()), is(false));
        assertThat(isOrganisationPopulated(Organisation.builder().organisationID(null).build()), is(false));
        assertThat(isOrganisationPopulated(Organisation.builder().organisationID("").build()), is(false));
        assertThat(isOrganisationPopulated(Organisation.builder().organisationName("populated").build()), is(false));
    }
}
