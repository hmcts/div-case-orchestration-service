package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
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
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.DN_PRONOUNCED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DA_PERIOD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch.CMSElasticSearchSupport.buildDateForTodayMinusGivenPeriod;

@Component
@Slf4j
public class SearchDNPronouncedCases implements Task<Map<String, Object>> {

    private static String DN_GRANTED_DATE = String.format("data.%s", DECREE_NISI_GRANTED_DATE_CCD_FIELD);

    private final CMSElasticSearchSupport cmsElasticSearchSupport;

    @Autowired
    public SearchDNPronouncedCases(CMSElasticSearchSupport cmsElasticSearchSupport) {
        this.cmsElasticSearchSupport = cmsElasticSearchSupport;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String coolOffPeriodInDN = context.getTransientObject(AWAITING_DA_PERIOD_KEY);

        QueryBuilder stateQuery = QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, DN_PRONOUNCED);
        String limitDate = buildDateForTodayMinusGivenPeriod(coolOffPeriodInDN);
        QueryBuilder dateFilter = QueryBuilders.rangeQuery(DN_GRANTED_DATE).lte(limitDate);

        try {
            List<String> caseIdList = cmsElasticSearchSupport.searchCMSCases(authToken, stateQuery, dateFilter)
                .map(CaseDetails::getCaseId)
                .collect(Collectors.toList());
            context.setTransientObject(SEARCH_RESULT_KEY, caseIdList);
        } catch (FeignException fException) {
            log.error("DN Pronounced cases eligible for DA search job failed: " + fException.getMessage(), fException);
            throw fException;
        }

        return payload;
    }

}
