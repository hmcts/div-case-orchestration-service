package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.LanguagePreference;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.email.EmailTemplateNames;
import uk.gov.hmcts.reform.divorce.orchestration.service.CaseOrchestrationServiceException;
import uk.gov.hmcts.reform.divorce.orchestration.service.EmailService;
import uk.gov.hmcts.reform.divorce.orchestration.service.NfdNotifierService;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.util.SearchForCaseByEmail;
import uk.gov.hmcts.reform.divorce.orchestration.util.nfd.IdamUser;
import uk.gov.hmcts.reform.divorce.orchestration.util.nfd.IdamUsersCsvLoader;
import uk.gov.hmcts.reform.divorce.orchestration.util.nfd.NfdAuthUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.nfd.NfdIdamService;
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
    protected static final String EMAIL_DESCRIPTION = "Divorce service to be retired notification - ";
    private final EmailService emailService;
    private final SearchForCaseByEmail searchForCaseByEmail;
    private final IdamUsersCsvLoader csvLoader;
    private final NfdIdamService nfdIdamService;
    private final NfdAuthUtil nfdAuthUtil;

    public NfdNotifierServiceImpl(EmailService emailService,
                                  SearchForCaseByEmail searchForCaseByEmail,
                                  IdamUsersCsvLoader csvLoader, NfdIdamService nfdIdamService, NfdAuthUtil nfdAuthUtil) {
        this.emailService = emailService;
        this.searchForCaseByEmail = searchForCaseByEmail;
        this.csvLoader = csvLoader;
        this.nfdIdamService = nfdIdamService;
        this.nfdAuthUtil = nfdAuthUtil;
    }


    @Override
    public void notifyUnsubmittedApplications() throws CaseOrchestrationServiceException {
        log.info("In the Notify Unsubmitted appplications job");
        String authToken = nfdAuthUtil.getCaseworkerToken();
        log.info("Got the token {}", authToken);
        List<IdamUser> idamUsers = csvLoader.loadIdamUserList("unsubmittedIdamUserList.csv");
        for (IdamUser idamUser : idamUsers) {
            try {
                checkUserHasSubmittedAndNotify(authToken, idamUser);
            } catch (RuntimeException e) {
                log.error("Error processing user {} ", idamUser.getIdamId());
                continue;
            }
        }
    }

    private void checkUserHasSubmittedAndNotify(String authToken, IdamUser idamUser) {
        UserDetails user = nfdIdamService.getUserDetail(idamUser.getIdamId(), authToken);
        if (user != null) {
            log.info("User details found for {} and email {}", user.getId(), user.getEmail());
            Optional<List<CaseDetails>> caseDetails = searchForCaseByEmail.searchCasesByEmail(user.getEmail());
            if (caseDetails.isEmpty()) {
                log.info("No case found for email {} so send a notification reminder", user.getEmail());
                Map<String, String> tempVars =
                    Map.of(SUBJECT, "Submit your divorce application", FIRSTNAME, user.getForename(), LASTNAME, user.getSurname().orElse(""));
                emailService.sendEmail(user.getEmail(), EmailTemplateNames.NFD_NOTIFICATION.name(), tempVars, EMAIL_DESCRIPTION,
                    LanguagePreference.ENGLISH);
            }
        }
    }

}
