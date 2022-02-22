package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.NfdIdamService;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.NfdNotifierService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.util.SearchForCaseByEmail;
import uk.gov.hmcts.reform.divorce.orchestration.util.nfd.IdamUser;
import uk.gov.hmcts.reform.divorce.orchestration.util.nfd.IdamUsersCsvLoader;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class NfdNotifierServiceImpl implements NfdNotifierService {

    protected static final String FIRSTNAME = "firstname";
    protected static final String LASTNAME = "lastname";
    protected static final String SUBJECT = "subject";
    protected static final String SUBMIT_YOUR_DIVORCE_APPLICATION = "Submit your divorce application";

    private final EmailService emailService;
    private final SearchForCaseByEmail searchForCaseByEmail;
    private final IdamUsersCsvLoader csvLoader;
    private final NfdIdamService nfdIdamService;
    private final String cutoffDate;

    protected static final String EMAIL_DESCRIPTION = "Divorce service to be retired notification - ";

    public NfdNotifierServiceImpl(EmailService emailService,
                                  SearchForCaseByEmail searchForCaseByEmail,
                                  IdamUsersCsvLoader csvLoader, NfdIdamService nfdIdamService, @Value("${nfd.cutoffdate}") String cutoffDate) {
        this.emailService = emailService;
        this.searchForCaseByEmail = searchForCaseByEmail;
        this.csvLoader = csvLoader;
        this.nfdIdamService = nfdIdamService;
        this.cutoffDate = cutoffDate;
    }

    @Override
    public void notifyUnsubmittedApplications(String authToken) {
        log.info("In the Notify Unsubmitted applications job with token {}", authToken);
        List<IdamUser> idamUsers = csvLoader.loadIdamUserList("unsubmittedIdamUserList.csv");
        for (IdamUser idamUser : idamUsers) {
            try {
                checkUserHasSubmittedAndNotify(authToken, idamUser);
            } catch (RuntimeException | CaseOrchestrationServiceException e) {
                log.error("Exception processing user " + idamUser.getIdamId(), e);
                continue;
            }
        }
    }

    private void checkUserHasSubmittedAndNotify(String authToken, IdamUser idamUser) throws CaseOrchestrationServiceException {
        UserDetails user = nfdIdamService.getUserDetail(idamUser.getIdamId(), authToken);
        if (user != null) {
            log.info("User details found for {} and email {}", user.getId(), user.getEmail());
            Optional<List<CaseDetails>> caseDetails = searchForCaseByEmail.searchCasesByEmail(user.getEmail());
            if (caseDetails.isEmpty()) {
                log.info("No case found for email {} so send a notification reminder", user.getEmail());
                Map<String, String> tempVars = Map.of(SUBJECT,
                    SUBMIT_YOUR_DIVORCE_APPLICATION, FIRSTNAME, user.getForename(), LASTNAME, user.getSurname().orElse(""));
                emailService.sendEmail(user.getEmail(), EmailTemplateNames.NFD_NOTIFICATION.name(), tempVars, EMAIL_DESCRIPTION,
                    LanguagePreference.ENGLISH);
            }
        }
    }
}
