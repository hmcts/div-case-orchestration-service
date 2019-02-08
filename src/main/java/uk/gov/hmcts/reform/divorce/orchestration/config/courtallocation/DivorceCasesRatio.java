package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_FIVE_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.UNREASONABLE_BEHAVIOUR;

/**
 * This is the configured values, given to us by the business for the predicted divorce ratio per fact.
 */
public class DivorceCasesRatio {

    private Map<String, BigDecimal> divorceRatioPerFact = new HashMap<>();

    public DivorceCasesRatio() {
        divorceRatioPerFact.put(UNREASONABLE_BEHAVIOUR, new BigDecimal("0.30"));
        divorceRatioPerFact.put(SEPARATION_TWO_YEARS, new BigDecimal("0.37"));
        divorceRatioPerFact.put(SEPARATION_FIVE_YEARS, new BigDecimal("0.21"));
        divorceRatioPerFact.put(DESERTION, new BigDecimal("0.11"));
        divorceRatioPerFact.put(ADULTERY, new BigDecimal("0.01"));
    }

    public Map<String, BigDecimal> getDivorceRatioPerFact() {
        return new HashMap<>(divorceRatioPerFact);
    }

}