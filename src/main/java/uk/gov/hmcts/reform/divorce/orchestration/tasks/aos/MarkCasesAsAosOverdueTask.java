package uk.gov.hmcts.reform.divorce.orchestration.tasks.aos;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueEvent;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.AosOverdueForProcessServerCaseEvent;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.AsyncTask;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.util.CMSElasticSearchSupport;
import uk.gov.hmcts.reform.divorce.orchestration.util.CaseOrchestrationValues;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.DUE_DATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.SERVED_BY_PROCESS_SERVER;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_AWAITING;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdStates.AOS_STARTED;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.CASE_STATE_JSON_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CMSElasticSearchSupport.ELASTIC_SEARCH_DAYS_REPRESENTATION;
import static uk.gov.hmcts.reform.divorce.orchestration.util.CMSElasticSearchSupport.buildDateForTodayMinusGivenPeriod;

@Component
@Slf4j
public class MarkCasesAsAosOverdueTask extends AsyncTask<Void> {

    @Autowired
    private CMSElasticSearchSupport cmsElasticSearchSupport;

    @Autowired
    private CaseOrchestrationValues caseOrchestrationValues;

    private QueryBuilder query;

    @PostConstruct
    public void init() {
        String aosOverdueGracePeriod = caseOrchestrationValues.getAosOverdueGracePeriod();
        log.info("Initialising {} with {} days of grace period.", MarkCasesAsAosOverdueTask.class.getSimpleName(), aosOverdueGracePeriod);
        String limitDate = buildDateForTodayMinusGivenPeriod(aosOverdueGracePeriod + ELASTIC_SEARCH_DAYS_REPRESENTATION);

        String elasticSearchMultiStateSearchQuery = String.join(SPACE, AOS_AWAITING, AOS_STARTED);

        query = QueryBuilders.boolQuery()
            .filter(QueryBuilders.matchQuery(CASE_STATE_JSON_KEY, elasticSearchMultiStateSearchQuery).operator(Operator.OR))
            .filter(QueryBuilders.rangeQuery("data." + DUE_DATE).lt(limitDate));
    }

    @Override
    public List<ApplicationEvent> getApplicationEvent(TaskContext context, Void payload) {
        List<ApplicationEvent> events = cmsElasticSearchSupport.searchCMSCasesWithSingleQuery(context.getTransientObject(AUTH_TOKEN_JSON_KEY), query)
            .map(caseDetails -> {
                ApplicationEvent eventToRaise;

                boolean caseServedByProcessServer = Optional.ofNullable(caseDetails.getCaseData())
                    .map(caseData -> caseData.get(SERVED_BY_PROCESS_SERVER))
                    .map(String.class::cast)
                    .map(YES_VALUE::equalsIgnoreCase)
                    .orElse(false);
                String caseId = caseDetails.getCaseId();

                if (caseServedByProcessServer) {
                    log.info("Case {} will be marked as AOS overdue (served by process server).", caseId);
                    eventToRaise = new AosOverdueForProcessServerCaseEvent(this, caseId);
                } else {
                    String state = caseDetails.getState();

                    if (AOS_STARTED.equalsIgnoreCase(state)) {
                        log.info("Case {} would have been marked as AOS Overdue, but it will be ignored because it's in {} state.",
                            caseId,
                            state);
                        eventToRaise = null;
                    } else {
                        log.info("Case {} will be marked as AOS Overdue.", caseId);
                        eventToRaise = new AosOverdueEvent(this, caseId);
                    }
                }

                return eventToRaise;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        log.info("Found {} cases for which AOS is overdue.", events.size());

        return events;
    }

}