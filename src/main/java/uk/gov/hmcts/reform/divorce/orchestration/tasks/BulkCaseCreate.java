package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import com.google.common.collect.ImmutableMap;
import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULKCASE_CREATION_ERROR;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_ACCEPTED_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.BULK_CASE_TITLE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_LIST_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_PARTIES_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.CASE_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.COST_ORDER_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.DN_APPROVAL_DATE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.FAMILY_MAN_REFERENCE_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.VALUE_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_APPROVAL_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_FIRST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.D_8_PETITIONER_LAST_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_FIRST_NAME_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.RESP_LAST_NAME_CCD_FIELD;

@Component
@Slf4j
@AllArgsConstructor
public class BulkCaseCreate implements Task<Map<String, Object>> {
    static final String BULK_CASE_TITLE = "Divorce bulk Case %s";

    private final CaseMaintenanceClient caseMaintenanceClient;

    private final CcdUtil ccdUtil;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) {
        List<SearchResult> searchResultList =  context.getTransientObject(SEARCH_RESULT_KEY);
        if (CollectionUtils.isEmpty(searchResultList)) {
            log.info("There is no cases to process");
            context.setTaskFailed(true);
            return Collections.emptyMap();
        }
        List<Object> errors = new ArrayList<>();
        List<Map<String, Object>> bulkCases = new ArrayList<>();
        searchResultList.forEach(searchResult -> {
            try {
                Map<String, Object> bulkCase = createBulkCase(searchResult);
                Map<String, Object> bulkCaseResult = caseMaintenanceClient.submitBulkCase(bulkCase,context.getTransientObject(AUTH_TOKEN_JSON_KEY));
                bulkCases.add(bulkCaseResult);
            } catch (FeignException e) {
                //Ignore bulk case creation failed. Next schedule should pickup the remaining cases
                // Still need to handle timeout, as the BulkCase could be created.
                errors.addAll(searchResult.getCases()
                    .stream()
                    .map(CaseDetails::getCaseId)
                    .collect(toList()));
                log.error("Bulk case creation failed.", e);
            }
        });
        Map<String, Object> cases = new HashMap<>();
        cases.put(BULK_CASE_LIST_KEY, bulkCases);
        if (!errors.isEmpty()) {
            context.setTransientObject(BULKCASE_CREATION_ERROR, errors);
        }
        return cases;
    }

    private Map<String, Object> createBulkCase(SearchResult searchResult) {
        List<Map<String, Object>> caseList = searchResult.getCases()
            .stream()
            .map(this::createCaseInBulkCase)
            .collect(toList());

        List<Map<String, Object>> acceptedCasesList = caseList.stream()
            .map(entry -> (Map<String, Object>)entry.get(VALUE_KEY))
            .map(entry -> entry.get(CASE_REFERENCE_FIELD))
            .map(entry -> ImmutableMap.of(VALUE_KEY, entry))
            .collect(toList());

        Map<String, Object> bulkCase = new HashMap<>();
        bulkCase.put(BULK_CASE_TITLE_KEY, String.format(BULK_CASE_TITLE, ccdUtil.getCurrentDateWithCustomerFacingFormat()));
        bulkCase.put(BULK_CASE_ACCEPTED_LIST_KEY, acceptedCasesList);
        bulkCase.put(CASE_LIST_KEY, caseList);

        return bulkCase;
    }

    private Map<String, Object> createCaseInBulkCase(CaseDetails caseDetails) {
        Map<String, Object> caseInBulk = new HashMap<>();

        caseInBulk.put(CASE_REFERENCE_FIELD, getCaseLink(caseDetails));
        caseInBulk.put(CASE_PARTIES_FIELD, getCaseParties(caseDetails));
        caseInBulk.put(FAMILY_MAN_REFERENCE_FIELD, caseDetails.getCaseData().get(D_8_CASE_REFERENCE));
        caseInBulk.put(COST_ORDER_FIELD, caseDetails.getCaseData().get(DIVORCE_COSTS_CLAIM_GRANTED_CCD_FIELD));
        caseInBulk.put(DN_APPROVAL_DATE_FIELD, caseDetails.getCaseData().get(DN_APPROVAL_DATE_CCD_FIELD));
        return ImmutableMap.of(VALUE_KEY, caseInBulk);
    }

    private Map<String, Object> getCaseLink(CaseDetails caseDetails) {
        return  ImmutableMap.of(CASE_REFERENCE_FIELD, caseDetails.getCaseId());
    }

    private String getCaseParties(CaseDetails caseDetails) {
        String petitionerFirstName = (String) caseDetails.getCaseData().get(D_8_PETITIONER_FIRST_NAME);
        String petitionerLastName = (String) caseDetails.getCaseData().get(D_8_PETITIONER_LAST_NAME);

        String respondentFirstName = (String) caseDetails.getCaseData().get(RESP_FIRST_NAME_CCD_FIELD);
        String respondentLastName = (String) caseDetails.getCaseData().get(RESP_LAST_NAME_CCD_FIELD);

        return  String.format("%s %s vs %s %s", petitionerFirstName, petitionerLastName, respondentFirstName,
            respondentLastName);
    }


}
