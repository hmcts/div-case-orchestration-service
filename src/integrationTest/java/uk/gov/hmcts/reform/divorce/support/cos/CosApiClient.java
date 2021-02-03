package uk.gov.hmcts.reform.divorce.support.cos;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.bsp.common.model.shared.in.ExceptionRecord;
import uk.gov.hmcts.reform.bsp.common.model.transformation.output.SuccessfulTransformationResponse;
import uk.gov.hmcts.reform.bsp.common.model.update.in.BulkScanCaseUpdateRequest;
import uk.gov.hmcts.reform.bsp.common.model.update.output.SuccessfulUpdateResponse;
import uk.gov.hmcts.reform.bsp.common.model.validation.in.OcrDataValidationRequest;
import uk.gov.hmcts.reform.divorce.context.ServiceContextConfiguration;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackRequest;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CcdCallbackResponse;

import java.util.Map;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.SERVICE_AUTHORIZATION_HEADER;

@FeignClient(name = "case-orchestration-api", url = "${case.orchestration.service.base.uri}",
    configuration = ServiceContextConfiguration.class)
public interface CosApiClient {

    @ApiOperation("Handle callback to trigger coRespReceived workflow")
    @PostMapping(value = "/co-respondent-received")
    Map<String, Object> coRespReceived(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody Map<String, Object> caseDataContent
    );

    @ApiOperation("Handle callback to trigger coRespAnswerReceived workflow")
    @PostMapping(value = "/co-respondent-answered")
    Map<String, Object> coRespAnswerReceived(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody CcdCallbackRequest caseDataContent
    );

    @ApiOperation("Handle callback to trigger aosReceived workflow")
    @PostMapping(value = "/aos-received")
    Map<String, Object> aosReceived(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody Map<String, Object> caseDataContent
    );

    @ApiOperation("Handle callback to trigger aosSubmitted workflow")
    @PostMapping(value = "/aos-submitted")
    Map<String, Object> aosSubmitted(
        @RequestBody Map<String, Object> caseDataContent);

    @ApiOperation("Handle callback to trigger aosSolicitorNominated workflow")
    @PostMapping(value = "/aos-solicitor-nominated")
    Map<String, Object> aosSolicitorNominated(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody Map<String, Object> caseDataContent
    );

    @ApiOperation("Handle callback to trigger dnSubmitted workflow")
    @PostMapping(value = "/dn-submitted")
    Map<String, Object> dnSubmitted(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody Map<String, Object> caseDataContent
    );

    @ApiOperation("Handle callback to trigger daAboutToBeGranted workflow")
    @PostMapping(value = "/da-about-to-be-granted")
    Map<String, Object> daAboutToBeGranted(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @ApiOperation("Callback to run after DA Grant event has finished")
    @PostMapping(value = "/handle-post-da-granted")
    ResponseEntity<CcdCallbackResponse> handleDaGranted(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @ApiOperation("Handle callback to notify respondent that the DA was requested")
    @PostMapping(value = "/da-requested-by-applicant")
    Map<String, Object> notifyRespondentOfDARequested(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @ApiOperation("Return a draft case")
    @GetMapping(value = "/draftsapi/version/1",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    Map<String, Object> getDraft(
        @RequestHeader(AUTHORIZATION) String authorisation
    );

    @ApiOperation("Delete a save a draft case")
    @PutMapping(value = "/draftsapi/version/1",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    void saveDraft(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody Map<String, Object> caseDataContent,
        @RequestParam(name = "sendEmail") String sendEmail
    );

    @ApiOperation("Delete a draft case")
    @DeleteMapping(value = "/draftsapi/version/1",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE)
    void deleteDraft(
        @RequestHeader(AUTHORIZATION) String authorisation
    );

    @ApiOperation("Submit a case")
    @PostMapping(value = "/submit")
    Map<String, Object> submitCase(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody Map<String, Object> caseDataContent
    );

    @ApiOperation("Handle callback to link a case to a hearing")
    @PostMapping(value = "/case-linked-for-hearing")
    Map<String, Object> caseLinkedForHearing(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody Map<String, Object> caseDataContent
    );

    @ApiOperation("Handle callback to submit Bulk Pronouncement")
    @PostMapping(value = "/bulk/pronounce/submit")
    Map<String, Object> bulkPronouncement(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @Deprecated
    @ApiOperation("Handle callback to edit bulk listing")
    @PostMapping(value = "/bulk/edit/listing")
    Map<String, Object> editBulkListing(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody CcdCallbackRequest ccdCallbackRequest,
        @RequestParam(name = "templateId") String templateId,
        @RequestParam(name = "documentType") String documentType,
        @RequestParam(name = "filename") String filename
    );

    @ApiOperation("Handle callback to generate documents with provided parameters")
    @PostMapping(value = "/generate-document")
    @Deprecated
    Map<String, Object> generateDocument(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody CcdCallbackRequest ccdCallbackRequest,
        @RequestParam(name = "templateId") String templateId,
        @RequestParam(name = "documentType") String documentType,
        @RequestParam(name = "filename") String filename
    );

    @ApiOperation("Prepare case data before printing for pronouncement")
    @PostMapping(value = "/prepare-to-print-for-pronouncement")
    Map<String, Object> prepareToPrintForPronouncement(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @ApiOperation("Prepare case data before updating bulk case hearing details")
    @PostMapping(value = "/update-bulk-case-hearing-details")
    Map<String, Object> updateBulkCaseHearingDetails(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @ApiOperation("Handle callback to generate DN Pronouncement Documents")
    @PostMapping(value = "/generate-dn-pronouncement-documents")
    Map<String, Object> generateDnPronouncedDocuments(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @ApiOperation("Handle callback to process Applicants' DA eligibility")
    @PostMapping(value = "/process-applicant-da-eligibility")
    CcdCallbackResponse processApplicantDAEligibility(
        @RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @ApiOperation("Handle callback submit DA for Case ID")
    @PostMapping(value = "/submit-da/{caseId}")
    Map<String, Object> submitDaCase(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody Map<String, Object> caseData,
        @PathVariable("caseId") String caseId
    );

    @ApiOperation("Handle callback to issue AOS Pack offline for provided parties")
    @PostMapping(value = "/issue-aos-pack-offline/parties/{party}")
    Map<String, Object> issueAosPackOffline(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @PathVariable("party") String party,
        @RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @ApiOperation("Handle callback to trigger offline AOS Answers for Respondent or Co-Respondent process")
    @PostMapping(value = "/processAosOfflineAnswers/parties/{party}")
    CcdCallbackResponse processAosPackOfflineAnswers(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @PathVariable("party") String party,
        @RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @ApiOperation("Handle callback to process Personal Service Pack")
    @PostMapping(value = "/personal-service-pack")
    CcdCallbackResponse processPersonalServicePack(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @ApiOperation("Handle callback to remove DN 'Outcome Case' flag")
    @PostMapping(value = "/remove-dn-outcome-case-flag")
    Map<String, Object> removeDnOutcomeCaseFlag(@RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @ApiOperation("Handle callback to remove LA 'Make Decision' fields")
    @PostMapping(value = "/remove-la-make-decision-fields")
    Map<String, Object> removeLegalAdvisorMakeDecisionFields(
        @RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @ApiOperation("Handle callback for DA about to be granted")
    @PostMapping(value = "/dn-about-to-be-granted")
    CcdCallbackResponse processDnAboutToBeGranted(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody CcdCallbackRequest ccdCallbackRequest
    );

    @ApiOperation("Handle callback for DN Decision Made")
    @PostMapping(value = "/dn-decision-made")
    CcdCallbackResponse dnDecisionMade(
        @RequestHeader(AUTHORIZATION) String authorisation,
        @RequestBody CcdCallbackRequest ccdCallbackRequest
    );


    @ApiOperation("Handle callback for Fee lookup")
    @PostMapping(value = "/set-up-confirm-service-payment")
    CcdCallbackResponse setupConfirmServicePayment(@RequestBody CcdCallbackRequest ccdCallbackRequest);

    @ApiOperation("Validate bulk scanned fields")
    @PostMapping(value = "/forms/{form-type}/validate-ocr")
    SuccessfulUpdateResponse validateBulkScannedFields(
        @RequestHeader(SERVICE_AUTHORIZATION_HEADER) String s2sAuthToken,
        @PathVariable("form-type") String formType,
        @RequestBody OcrDataValidationRequest request
    );

    @ApiOperation("Transform bulk scanned fields to CCD format")
    @PostMapping(value = "/transform-exception-record")
    SuccessfulTransformationResponse transformBulkScannedFields(
        @RequestHeader(SERVICE_AUTHORIZATION_HEADER) String s2sAuthToken,
        @RequestBody ExceptionRecord exceptionRecord
    );

    @ApiOperation("Transform bulk scanned fields to CCD format for updating case")
    @PostMapping(value = "/update-case")
    SuccessfulUpdateResponse transformBulkScannedFieldsForUpdatingCase(
        @RequestHeader(SERVICE_AUTHORIZATION_HEADER) String s2sAuthToken,
        @RequestBody BulkScanCaseUpdateRequest request
    );
}