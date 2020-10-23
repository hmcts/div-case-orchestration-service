package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalorders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.model.ccd.DivorceGeneralOrder;
import uk.gov.hmcts.reform.divorce.model.parties.DivorceParty;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.CcdUtil;
import uk.gov.hmcts.reform.divorce.orchestration.util.UserDivorcePartyLookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDERS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.AUTH_TOKEN_JSON_KEY;

@RequiredArgsConstructor
@Component
@Slf4j
public class GeneralOrdersFilterTask implements Task<Map<String, Object>> {

    private final UserDivorcePartyLookup userDivorcePartyLookup;

    private final CcdUtil ccdUtil;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> caseData) throws TaskException {
        String authToken = context.getTransientObject(AUTH_TOKEN_JSON_KEY);
        log.info("Looking up divorce party for user");
        Optional<DivorceParty> divorceParty = userDivorcePartyLookup.lookupDivorcePartForGivenUser(authToken, caseData);
        String divorcePartyDescription = divorceParty.map(DivorceParty::getDescription).orElse("[none]");
        log.info("Found divorce party {} for user", divorcePartyDescription);

        Map<String, Object> newCaseData = new HashMap<>(caseData);
        if (newCaseData.containsKey(GENERAL_ORDERS)) {
            List<CollectionMember<DivorceGeneralOrder>> allGeneralOrders = ccdUtil.getListOfCollectionMembers(newCaseData, GENERAL_ORDERS);
            log.info("Found {} general orders.", allGeneralOrders.size());

            List<CollectionMember<DivorceGeneralOrder>> filteredGeneralOrders = divorceParty
                .map(party -> allGeneralOrders.stream()
                    .filter(go -> go.getValue().getGeneralOrderParties().contains(party))
                    .collect(Collectors.toList())
                )
                .orElse(new ArrayList<>());

            newCaseData.put(GENERAL_ORDERS, filteredGeneralOrders);
            log.info("{} general orders were left after filtering for {} party.",
                allGeneralOrders.size(),
                divorcePartyDescription);
        } else {
            log.info("{} field not found in case data", GENERAL_ORDERS);
        }

        return newCaseData;
    }

}