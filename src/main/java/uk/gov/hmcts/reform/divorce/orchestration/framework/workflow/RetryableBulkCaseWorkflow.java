package uk.gov.hmcts.reform.divorce.orchestration.framework.workflow;

import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.orchestration.exception.BulkUpdateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_ACCEPTED_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CCD_CASE_DATA_FIELD;

/**
 * RetryableBulkCaseWorkflow
 * Updates individual divorce cases accepted within a bulk case.
 */
@Slf4j
public abstract class RetryableBulkCaseWorkflow extends DefaultWorkflow<Map<String, Object>> {

    @Value("${bulk-action.retries.max:4}")
    private int maxRetries;

    @Value("${bulk-action.retries.backoff.base-rate:1000}")
    private int backoffBaseRate;

    /**
     * Process a specific case within a bulk case.
     *
     * @param bulkCaseData BulkCaseDetails in ccd format
     * @param caseId Divorce case id
     * @param authToken Caseworker auth token
     * @return workflow result
     * @throws WorkflowException when workflow execution fails
     */
    public abstract Map<String, Object> run(Map<String, Object> bulkCaseData, String caseId, String authToken) throws WorkflowException;

    /**
     *  Process accepted cases, CaseAcceptedList,  within the bulk case.
     *  If updating any case fails with 5xx error in CCD the process will retry the case.
     *  The process will not retry the case if CCD fails with 4xx error.
     *
     * @param bulkCaseResponse Bulk case data in CCD format
     * @param bulkCaseId Bulk case id
     * @param authToken Caseworker auth token
     * @return true if all the case have been updated successfully, false if any case failed.
     */
    public boolean executeWithRetries(Map<String, Object> bulkCaseResponse, String bulkCaseId, String authToken) {
        Map<String, Object> bulkCaseData = (Map<String, Object>) bulkCaseResponse.getOrDefault(CCD_CASE_DATA_FIELD, Collections.emptyMap());
        List<Map<String, Object>> acceptedDivorceCaseList =
            (List<Map<String, Object>>) bulkCaseData.getOrDefault(BULK_CASE_ACCEPTED_LIST_KEY, Collections.emptyList());

        if (acceptedDivorceCaseList.isEmpty()) {
            throw new BulkUpdateException("Accepted case list is empty. Not updating bulk case");
        }

        int retryCount = 0;
        List<Map<String, Object>> retryCases = acceptedDivorceCaseList;
        final List<Map<String, Object>> failedCases = new ArrayList<>();
        try {
            while (!retryCases.isEmpty() && retryCount < maxRetries) {
                if (retryCount > 0) {
                    exponentialWaitTime(retryCount);
                }
                retryCases = handleFailedCases(retryCases, bulkCaseId, authToken, failedCases, bulkCaseResponse);
                retryCount ++;
            }

            if (!retryCases.isEmpty()) {
                failedCases.addAll(retryCases);
            }
        } finally {
            if (!failedCases.isEmpty()) {
                this.notifyFailedCases(bulkCaseId, failedCases);
            }
        }
        return failedCases.isEmpty();
    }

    private List<Map<String, Object>> handleFailedCases(List<Map<String, Object>> caseList,
                                                        String bulkCaseId,
                                                        String authToken,
                                                        List<Map<String, Object>> nonRetryableCases,
                                                        Map<String, Object> bulkCaseData) {
        final List<Map<String, Object>> casesToRetry = new ArrayList<>();

        caseList.forEach(caseElem -> {
            String caseId = "";
            try {
                Map<String, Object> caseLink = (Map<String, Object>) caseElem.get(VALUE_KEY);
                caseId = String.valueOf(caseLink.get(CASE_REFERENCE_FIELD));
                this.run(bulkCaseData, caseId, authToken);
            } catch (FeignException.BadGateway
                | FeignException.InternalServerError
                | FeignException.GatewayTimeout
                | FeignException.ServiceUnavailable
                | RetryableException e) {
                String errorMessage = e.content() == null ? e.getMessage() : e.contentUTF8();
                log.error("Case update failed, added to retry list: for bulk case id {} and caseId {}. Cause {}",
                    bulkCaseId, caseId, errorMessage, e);
                casesToRetry.add(caseElem);
            } catch (Exception e) {
                log.error("Case update failed : for bulk case id {}  and caseId {}", bulkCaseId, caseId, e);
                nonRetryableCases.add(caseElem);
            }
        });
        return casesToRetry;
    }

    /**
     * Exponential back off
     * Retries in 2, 4, 8... seconds.
     * @param retryCount repeat iteration
     */
    private void exponentialWaitTime(int retryCount) {
        long waitTime = (long) Math.pow(2, retryCount);
        try {
            log.error("Re-trying bulkCase with waiting time {} seconds", waitTime);
            Thread.sleep(waitTime * backoffBaseRate);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread sleep interrupted", e);
        }
    }

    void notifyFailedCases(String bulkCaseId, List<Map<String, Object>> failedCases) {
        log.error("Bulk case update failed with bulkCaseId {} and failing cases {}", bulkCaseId,  failedCases);
    }
}