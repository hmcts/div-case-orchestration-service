package uk.gov.hmcts.reform.divorce.orchestration.tasks.aos;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CaseDetails;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueRequest;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.AsyncTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CMSElasticSearchSupport;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseOrchestrationValues;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CMSElasticSearchSupport.ELASTIC_SEARCH_DAYS_REPRESENTATION;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CMSElasticSearchSupport.buildDateForTodayMinusGivenPeriod;

@Component
@Slf4j
public class MarkAosCasesAsOverdueTask extends AsyncTask<Void> {

    @Autowired
    private CMSElasticSearchSupport cmsElasticSearchSupport;

    @Autowired
    private CaseOrchestrationValues caseOrchestrationValues;

    private QueryBuilder query;

    @PostConstruct
    public void init() {
        String aosOverdueGracePeriod = caseOrchestrationValues.getAosOverdueGracePeriod();
        log.info("Initialising {} with {} days of grace period.", MarkAosCasesAsOverdueTask.class.getSimpleName(), aosOverdueGracePeriod);
        String limitDate = buildDateForTodayMinusGivenPeriod(aosOverdueGracePeriod + ELASTIC_SEARCH_DAYS_REPRESENTATION);
        query = QueryBuilders.boolQuery()
            .filter(QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, AOS_AWAITING))
            .filter(QueryBuilders.rangeQuery("data.dueDate").lt(limitDate))
            .mustNot(QueryBuilders.matchQuery("data.ServedByProcessServer", YES_VALUE));
    }

    @Override
    public List<ApplicationEvent> getApplicationEvent(TaskContext context, Void payload) {
        List<ApplicationEvent> events = cmsElasticSearchSupport.searchCMSCasesWithSingleQuery(context.getTransientObject(AUTH_TOKEN_JSON_KEY), query)
            .map(CaseDetails::getCaseId)
            .map(caseId -> new AosOverdueRequest(this, caseId))
            .collect(Collectors.toList());

        log.info("Found {} cases eligible to be moved to AOS Overdue.", events.size());

        return events;
    }

}