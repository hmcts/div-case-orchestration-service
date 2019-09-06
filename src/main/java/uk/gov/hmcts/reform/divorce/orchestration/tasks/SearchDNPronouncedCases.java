package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.client.CaseMaintenanceClient;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.tasks.transformation.CaseIdMapper;
import uk.gov.hmcts.reform.divorce.orchestration.util.CMSHelper;

import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.BulkCaseConstants.SEARCH_RESULT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AWAITING_DA_PERIOD_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DECREE_NISI_GRANTED_DATE_CCD_FIELD;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.DN_PRONOUNCED;

@Component
@Slf4j
public class SearchDNPronouncedCases implements Task<Map<String, Object>> {

    private static String DN_GRANTED_DATE = String.format("data.%s", DECREE_NISI_GRANTED_DATE_CCD_FIELD);

    private final CaseMaintenanceClient caseMaintenanceClient;
    private final CMSHelper cmsHelper;

    @Autowired
    public SearchDNPronouncedCases(CaseMaintenanceClient caseMaintenanceClient, CaseIdMapper caseDetailsMapper) {
        this.caseMaintenanceClient = caseMaintenanceClient;
        this.cmsHelper = new CMSHelper(this.caseMaintenanceClient, caseDetailsMapper);
    }

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        int start = context.<Integer>getTransientObjectOptional("FROM").orElse(0);
        int pageSize = context.<Integer>getTransientObjectOptional("PAGE_SIZE").orElse(50);
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        String coolOffPeriodInDN = context.getTransientObject(AWAITING_DA_PERIOD_KEY);

        QueryBuilder stateQuery = QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, DN_PRONOUNCED);
        QueryBuilder dateFilter = QueryBuilders.rangeQuery(DN_GRANTED_DATE).lte(buildCoolOffPeriodInDNBoundary(coolOffPeriodInDN));

        try {
            List<String> caseIdList = cmsHelper.searchCMSCases(start, pageSize, authToken, stateQuery, dateFilter);
            context.setTransientObject(SEARCH_RESULT_KEY, caseIdList);
        } catch (FeignException fException) {
            log.error("DN Pronounced cases eligible for DA search job failed: " + fException.getMessage(), fException);
            throw fException;
        }

        return payload;
    }

    private String buildCoolOffPeriodInDNBoundary(final String coolOffPeriodInDN) {
        String timeUnit = String.valueOf(coolOffPeriodInDN.charAt(coolOffPeriodInDN.length() - 1));
        return String.format("now/%s-%s", timeUnit, coolOffPeriodInDN);
    }

}