package uk.gov.hmcts.reform.divorce.support.cos;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.divorce.context.ServiceContextConfiguration;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;

import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "case-orchestration-api", url = "${case.orchestration.service.base.uri}",
    configuration = ServiceContextConfiguration.class)
public interface CosApiClient {

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/co-respondent-received"
    )
    Map<String, Object> coRespReceived(@RequestHeader(AUTHORIZATION) String authorisation,
                                       @RequestBody Map<String, Object> caseDataContent
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/co-respondent-answered"
    )
    Map<String, Object> coRespAnswerReceived(@RequestHeader(AUTHORIZATION) String authorisation,
                                             @RequestBody CcdCallbackRequest caseDataContent
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/aos-received"
    )
    Map<String, Object> aosReceived(@RequestHeader(AUTHORIZATION) String authorisation,
                                    @RequestBody Map<String, Object> caseDataContent
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/aos-submitted"
    )
    Map<String, Object> aosSubmitted(@RequestBody Map<String, Object> caseDataContent);

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/aos-solicitor-nominated"
    )
    Map<String, Object> aosSolicitorNominated(@RequestBody Map<String, Object> caseDataContent);

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/dn-submitted"
    )
    Map<String, Object> dnSubmitted(@RequestHeader(AUTHORIZATION) String authorisation,
                                    @RequestBody Map<String, Object> caseDataContent
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/da-about-to-be-granted"
    )
    Map<String, Object> daAboutToBeGranted(@RequestHeader(AUTHORIZATION) String authorisation,
                                           @RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/da-requested-by-applicant"
    )
    Map<String, Object> notifyRespondentOfDARequested(@RequestHeader(AUTHORIZATION) String authorisation,
                                                      @RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @RequestMapping(
        method = RequestMethod.GET,
        value = "/draftsapi/version/1",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> getDraft(@RequestHeader(AUTHORIZATION) String authorisation);

    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/draftsapi/version/1",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    void saveDraft(@RequestHeader(AUTHORIZATION) String authorisation,
                   @RequestBody JsonNode caseDataContent,
                   @RequestParam(name = "sendEmail") String sendEmail
    );

    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/draftsapi/version/1",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    void deleteDraft(@RequestHeader(AUTHORIZATION) String authorisation);

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/submit"
    )
    Map<String, Object> submitCase(@RequestHeader(AUTHORIZATION) String authorisation,
                                   @RequestBody JsonNode caseDataContent
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/case-linked-for-hearing"
    )
    Map<String, Object> caseLinkedForHearing(@RequestHeader(AUTHORIZATION) String authorisation,
                                             @RequestBody Map<String, Object> caseDataContent);

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/bulk/pronounce/submit"
    )
    Map<String, Object> bulkPronouncement(@RequestHeader(AUTHORIZATION) String authorisation,
                                          @RequestBody CcdCallbackRequest ccdCallbackRequest);

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/bulk/edit/listing"
    )
    Map<String, Object> editBulkListing(@RequestHeader(AUTHORIZATION) String authorisation,
                                        @RequestBody CcdCallbackRequest ccdCallbackRequest,
                                        @RequestParam(name = "templateId") String templateId,
                                        @RequestParam(name = "documentType") String documentType,
                                        @RequestParam(name = "filename") String filename);

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/generate-document"
    )
    Map<String, Object> generateDocument(@RequestHeader(AUTHORIZATION) String authorisation,
                                         @RequestBody CcdCallbackRequest ccdCallbackRequest,
                                         @RequestParam(name = "templateId") String templateId,
                                         @RequestParam(name = "documentType") String documentType,
                                         @RequestParam(name = "filename") String filename);

    @RequestMapping(
        method = RequestMethod.POST,
        value = "generate-dn-pronouncement-documents"
    )
    Map<String, Object> generateDnPronouncedDocuments(@RequestHeader(AUTHORIZATION) String authorisation,
                                                      @RequestBody CcdCallbackRequest ccdCallbackRequest);


    @RequestMapping(
        method = RequestMethod.POST,
        value = "/process-applicant-da-eligibility"
    )
    CcdCallbackResponse processApplicantDAEligibility(@RequestBody CcdCallbackRequest ccdCallbackRequest);

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/submit-da/{caseId}"
    )
    Map<String, Object> submitDaCase(@RequestHeader(AUTHORIZATION) String authorisation,
                                     @RequestBody Map<String, Object> caseData,
                                     @PathVariable("caseId") String caseId
    );

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/issue-aos-pack-offline/party/{party}"
    )
    Map<String, Object> issueAosPackOffline(@RequestHeader(AUTHORIZATION) String authorisation,
                                            @PathVariable("party") String party,
                                            @RequestBody CcdCallbackRequest ccdCallbackRequest);


    @RequestMapping(
            method = RequestMethod.POST,
            value = "/personal-service-pack"
    )
    CcdCallbackResponse processPersonalServicePack(
            @RequestHeader(AUTHORIZATION) String authorisation,
            @RequestBody CcdCallbackRequest ccdCallbackRequest
    );
}
