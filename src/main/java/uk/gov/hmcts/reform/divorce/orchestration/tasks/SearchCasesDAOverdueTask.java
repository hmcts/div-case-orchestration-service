package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch.CMSElasticSearchSupport;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AWAITING_DA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;

@Component
@Slf4j
public class SearchCasesDAOverdueTask implements Task<Map<String, Object>> {

    private static final String DN_GRANTED_DATE = String.format("data.%s", DECREE_NISI_GRANTED_DATE_CCD_FIELD);

    private final String daOverdueTimeLimit;
    private final CMSElasticSearchSupport cmsElasticSearchSupport;

    public SearchCasesDAOverdueTask(@Autowired CMSElasticSearchSupport cmsElasticSearchSupport,
                                    @Value("${case.event.da-overdue-period:1y}") String daOverdueTimeLimit) {
        this.cmsElasticSearchSupport = cmsElasticSearchSupport;
        this.daOverdueTimeLimit = daOverdueTimeLimit;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);

        QueryBuilder stateQuery = QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, AWAITING_DA);
        String limitDate = CMSElasticSearchSupport.buildDateForTodayMinusGivenPeriod(daOverdueTimeLimit);
        QueryBuilder dateFilter = QueryBuilders.rangeQuery(DN_GRANTED_DATE).lte(limitDate);

        try {
            List<String> caseIdList = cmsElasticSearchSupport.searchCMSCases(authToken, stateQuery, dateFilter)
                .map(CaseDetails::getCaseId)
                .collect(Collectors.toList());
            context.setTransientObject(SEARCH_RESULT_KEY, caseIdList);
        } catch (FeignException fException) {
            log.error("DA Overdue search job failed: " + fException.getMessage(), fException);
            throw fException;
        }

        return payload;
    }

}