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
import uk.gov.hmcts.reform.divorce.orchestration.util.CMSElasticSearchSupport;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DA;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;

@Component
@Slf4j
public class SearchCasesDAOverdueTask implements Task<Map<String, Object>> {

    @Value("${case.event.da-overdue-period:1y}")
    private String daOverduePeriod;

    private static final String DN_GRANTED_DATE = String.format("data.%s", DECREE_NISI_GRANTED_DATE_CCD_FIELD);

    private final CMSElasticSearchSupport cmsElasticSearchSupport;

    @Autowired
    public SearchCasesDAOverdueTask(CMSElasticSearchSupport cmsElasticSearchSupport) {
        this.cmsElasticSearchSupport = cmsElasticSearchSupport;
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {

        int start = context.<Integer>getTransientObjectOptional("FROM").orElse(0);
        int pageSize = context.<Integer>getTransientObjectOptional("PAGE_SIZE").orElse(50);
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);

        QueryBuilder stateQuery = QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, AWAITING_DA);
        QueryBuilder dateFilter = QueryBuilders.rangeQuery(DN_GRANTED_DATE).lte(buildCoolOffPeriodForDAOverdue(daOverduePeriod));

        try {
            List<String> caseIdList = cmsElasticSearchSupport.searchCMSCases(start, pageSize, authToken, stateQuery, dateFilter)
                .map(CaseDetails::getCaseId)
                .collect(Collectors.toList());
            context.setTransientObject(SEARCH_RESULT_KEY, caseIdList);
        } catch (FeignException fException) {
            log.error("DA Overdue search job failed: " + fException.getMessage(), fException);
            throw fException;
        }

        return payload;
    }

    private static String buildCoolOffPeriodForDAOverdue(final String coolOffPeriod) {
        String timeUnit = String.valueOf(coolOffPeriod.charAt(coolOffPeriod.length() - 1));
        return String.format("now/%s-%s", timeUnit, coolOffPeriod);
    }
}
