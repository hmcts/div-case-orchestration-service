package uk.gov.hmcts.reform.divorce.orchestration.domain.model.helper;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.RESPONDENT_SOLICITOR_ORGANISATION_POLICY;

public class DigitalSolicitorOrganisationHelper {

    public static boolean isSolicitorRegistered(Map<String, Object> caseData) {
        boolean solicitorRegistered = Optional.ofNullable(caseData)
            .map(m -> m.get(RESPONDENT_SOLICITOR_ORGANISATION_POLICY))
            .map(Map.class::cast)
            .map(m -> m.get("Organisation"))
            .map(Map.class::cast)
            .map(m -> m.get("OrganisationID"))
            .map(String.class::cast)
            .filter(StringUtils::isNotBlank)
            .isPresent();
        return solicitorRegistered;
    }

}