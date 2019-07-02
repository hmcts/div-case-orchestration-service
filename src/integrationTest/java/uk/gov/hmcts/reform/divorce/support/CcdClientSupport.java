package uk.gov.hmcts.reform.divorce.support;

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
    private String divorceCaseType;

    @Value("${ccd.bulk.casetype}")
    private String bulkCaseType;

    @Value("${ccd.eventid.create}")
    private String createEventId;

    @Value("${ccd.eventid.solicitorCreate}")
    private String solicitorCreateEventId;

    @Value("${ccd.bulk.eventid.create}")
    private String bulkCreateEvent;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    @Qualifier("ccdSubmissionTokenGenerator")
    private AuthTokenGenerator authTokenGenerator;

    public CaseDetails submitCaseForCitizen(Object data, UserDetails userDetails) {
        return submitCaseForCitizenAsEvent(data, userDetails, divorceCaseType);
    }

    public CaseDetails submitCaseForSolicitor(Object data, UserDetails userDetails) {
        return submitCaseForCaseWorkerAsEvent(data, userDetails, divorceCaseType, solicitorCreateEventId);
    }

    public CaseDetails submitBulkCase(Object data, UserDetails userDetails) {
        return submitCaseForCaseWorkerAsEvent(data, userDetails, bulkCaseType, bulkCreateEvent);
    }

    private CaseDetails submitCaseForCitizenAsEvent(Object data, UserDetails userDetails, String caseType) {
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

    private CaseDetails submitCaseForCaseWorkerAsEvent(Object data, UserDetails userDetails, String caseType, String eventId) {
        final String serviceToken = authTokenGenerator.generate();

        StartEventResponse startEventResponse = coreCaseDataApi.startForCaseworker(
            userDetails.getAuthToken(),
            serviceToken,
            userDetails.getId(),
            jurisdictionId,
            caseType,
            eventId);

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

        return coreCaseDataApi.submitForCaseworker(
            userDetails.getAuthToken(),
            serviceToken,
            userDetails.getId(),
            jurisdictionId,
            caseType,
            true,
            caseDataContent);
    }

    CaseDetails updateForCitizen(String caseId, Object data, String eventId, UserDetails userDetails) {
        final String serviceToken = authTokenGenerator.generate();

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCitizen(
            userDetails.getAuthToken(),
            serviceToken,
            userDetails.getId(),
            jurisdictionId,
            divorceCaseType,
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

        return coreCaseDataApi.submitEventForCitizen(
            userDetails.getAuthToken(),
            serviceToken,
            userDetails.getId(),
            jurisdictionId,
            divorceCaseType,
            caseId,
            true,
            caseDataContent);
    }

    public CaseDetails update(String caseId, Object data, String eventId, UserDetails userDetails) {
        return update(caseId, data, eventId, userDetails, false);
    }

    CaseDetails update(String caseId, Object data, String eventId, UserDetails userDetails, boolean isBulkType) {
        final String serviceToken = authTokenGenerator.generate();

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            userDetails.getAuthToken(),
            serviceToken,
            userDetails.getId(),
            jurisdictionId,
            isBulkType ? bulkCaseType : divorceCaseType,
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
            isBulkType ? bulkCaseType : divorceCaseType,
            caseId,
            true,
            caseDataContent);
    }

    public CaseDetails retrieveCase(UserDetails userDetails, String caseId) {
        return coreCaseDataApi.readForCitizen(
            userDetails.getAuthToken(),
            authTokenGenerator.generate(),
            userDetails.getId(),
            jurisdictionId,
            divorceCaseType,
            caseId);
    }

    public CaseDetails retrieveCaseForCaseworker(UserDetails userDetails, String caseId) {
        return coreCaseDataApi.readForCaseWorker(
            userDetails.getAuthToken(),
            authTokenGenerator.generate(),
            userDetails.getId(),
            jurisdictionId,
            divorceCaseType,
            caseId);
    }

}
