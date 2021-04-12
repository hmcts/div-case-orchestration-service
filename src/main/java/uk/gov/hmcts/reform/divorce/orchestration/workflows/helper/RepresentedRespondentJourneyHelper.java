package uk.gov.hmcts.reform.divorce.orchestration.workflows.helper;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.Features;
import uk.gov.hmcts.reform.divorce.orchestration.service.FeatureToggleService;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentRepresented;
import static uk.gov.hmcts.reform.divorce.orchestration.util.PartyRepresentationChecker.isRespondentSolicitorDigital;

/**
 * Collection of methods to help implement the logic behind the Represented Respondent Journey epic
 * without further burdening the classes that need this information.
 * The idea here is to have very specific business questions that other parts of the system can rely on.
 * This way we can reduce complexity in already complex classes.
 */
@AllArgsConstructor
@Slf4j
@Component
public class RepresentedRespondentJourneyHelper {

    private final FeatureToggleService featureToggleService;

    public boolean shouldGenerateRespondentAosInvitation(Map<String, Object> caseData) {
        boolean shouldGenerateRespondentAosInvitation = true;

        boolean featureEnabled = isRepresentedRespondentJourneyEnabled();

        if (featureEnabled) {
            log.info("REPRESENTED_RESPONDENT_JOURNEY enabled.");
            boolean respondentRepresented = isRespondentRepresented(caseData);
            boolean respondentSolicitorDigital = isRespondentSolicitorDigital(caseData);
            if (respondentRepresented && !respondentSolicitorDigital) {
                shouldGenerateRespondentAosInvitation = false;
            }
        } else {
            log.info("REPRESENTED_RESPONDENT_JOURNEY not enabled.");
        }

        return shouldGenerateRespondentAosInvitation;
    }

    public boolean shouldUpdateNoticeOfProceedingsDetails(Map<String, Object> caseData) {
        return isRepresentedRespondentJourneyEnabled() && isRespondentSolicitorDigital(caseData);
    }

    private boolean isRepresentedRespondentJourneyEnabled() {
        return featureToggleService.isFeatureEnabled(Features.REPRESENTED_RESPONDENT_JOURNEY);
    }

}