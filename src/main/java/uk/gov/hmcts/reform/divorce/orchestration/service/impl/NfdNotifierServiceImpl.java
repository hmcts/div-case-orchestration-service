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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;

@Slf4j
@Component
public class NfdNotifierServiceImpl implements NfdNotifierService {

    protected static final String FIRSTNAME = "firstname";
    protected static final String LASTNAME = "lastname";
    protected static final String SUBJECT = "subject";

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
        log.info("In the Notify Unsubmitted appplications job");
        List<IdamUser> idamUsers = csvLoader.loadIdamUserList("unsubmittedIdamUserList.csv");
        for (IdamUser idamUser : idamUsers) {
            try {
                checkUserHasSubmittedAndNotify(authToken, idamUser);
            } catch (RuntimeException | CaseOrchestrationServiceException e) {
                log.error("Error processing  user {}", idamUser.getIdamId());
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
                Map<String, String> tempVars = Map.of(SUBJECT, getSubject(), FIRSTNAME, user.getForename(), LASTNAME, user.getSurname().orElse(""));
                emailService.sendEmail(user.getEmail(), EmailTemplateNames.NFD_NOTIFICATION.name(), tempVars, EMAIL_DESCRIPTION,
                    LanguagePreference.ENGLISH);
            }
        }
    }

    protected String getSubject() throws CaseOrchestrationServiceException {
        Date today = new Date();
        Date dateCutOff;
        try {
            dateCutOff = new SimpleDateFormat("dd-MM-yyyy").parse(cutoffDate);
        } catch (ParseException e) {
            throw new CaseOrchestrationServiceException(e);
        }

        Instant start = today.toInstant();
        Instant end = dateCutOff.toInstant().plus(1, DAYS);

        long daysBetween = Duration.between(start, end).toDays();
        if (daysBetween > 0) {
            return daysBetween + " days to complete your divorce application";
        }
        return "last chance to complete your divorce application";

    }
}
