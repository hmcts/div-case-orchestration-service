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

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CMSElasticSearchSupport.buildDateForTodayMinusGivenPeriod;

@Component
@Slf4j
public class MarkAosCasesAsOverdueTask extends AsyncTask<Void> {

    private static final String AOS_TIME_LIMIT = "30d";

    @Autowired
    private CMSElasticSearchSupport cmsElasticSearchSupport;

    private QueryBuilder[] queryBuilders;

    @PostConstruct
    public void init() {
        String limitDate = buildDateForTodayMinusGivenPeriod(AOS_TIME_LIMIT);
        queryBuilders = new QueryBuilder[] {
            QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, AOS_AWAITING),
            QueryBuilders.rangeQuery("data.dueDate").lte(limitDate)
        };
    }

    @Override
    public List<ApplicationEvent> getApplicationEvent(TaskContext context, Void payload) {
        List<ApplicationEvent> events = cmsElasticSearchSupport.searchCMSCases(context.getTransientObject(AUTH_TOKEN_JSON_KEY), queryBuilders)
            .map(CaseDetails::getCaseId)
            .map(caseId -> new AosOverdueRequest(this, caseId))
            .collect(Collectors.toList());

        log.info("Found {} cases eligible to be moved to AOS Overdue.");

        return events;
    }

}