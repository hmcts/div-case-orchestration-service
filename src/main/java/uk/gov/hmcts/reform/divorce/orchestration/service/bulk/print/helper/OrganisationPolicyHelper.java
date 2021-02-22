package uk.gov.hmcts.reform.divorce.orchestration.service.bulk.print.helper;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Organisation;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.OrganisationPolicy;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OrganisationPolicyHelper {

    public static boolean isOrganisationPolicyPopulated(OrganisationPolicy organisationPolicy) {
        return organisationPolicy != null && isOrganisationPopulated(organisationPolicy.getOrganisation());
    }

    public static boolean isOrganisationPopulated(Organisation organisation) {
        return organisation != null && !Strings.isNullOrEmpty(organisation.getOrganisationID());
    }
}
