package uk.gov.hmcts.reform.divorce.support.ccd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.model.UserDetails;

public class CcdClientSupport {
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "Divorce case submission event";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting Divorce Case";

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @Value("${ccd.eventid.create}")
    private String createEventId;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    @Qualifier("ccdSubmissionTokenGenerator")
    private AuthTokenGenerator authTokenGenerator;

    public CaseDetails submitCase(Object data, UserDetails userDetails) {
        final String serviceToken = authTokenGenerator.generate();

        StartEventResponse startEventResponse = coreCaseDataApi.startForCitizen(
            userDetails.getAuthToken(),
            serviceToken,
            userDetails.getId(),
            jurisdictionId,
            caseType,
            createEventId);

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(data)
            .build();

        return coreCaseDataApi.submitForCitizen(
            userDetails.getAuthToken(),
            serviceToken,
            userDetails.getId(),
            jurisdictionId,
            caseType,
            true,
            caseDataContent);
    }


    public CaseDetails update(String caseId, Object data, String eventId, UserDetails userDetails) {
        final String serviceToken = authTokenGenerator.generate();

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            userDetails.getAuthToken(),
            serviceToken,
            userDetails.getId(),
            jurisdictionId,
            caseType,
            caseId,
            eventId);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(data)
            .build();

        return coreCaseDataApi.submitEventForCaseWorker(
            userDetails.getAuthToken(),
            serviceToken,
            userDetails.getId(),
            jurisdictionId,
            caseType,
            caseId,
            true,
            caseDataContent);
    }
}
