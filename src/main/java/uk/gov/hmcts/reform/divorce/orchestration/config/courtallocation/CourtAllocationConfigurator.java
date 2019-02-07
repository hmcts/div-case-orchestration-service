package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocationConfiguration;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocator;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.DefaultCourtAllocator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.CourtConstants.SELECTED_COURT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_FIVE_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFacts.UNREASONABLE_BEHAVIOUR;

@Configuration
public class CourtAllocationConfigurator {

    @Autowired
    private CourtAllocationConfiguration courtAllocationConfig;

    @Bean
    public CourtAllocator configureCourtAllocationFromEnvironmentVariable() {
        return new DefaultCourtAllocator(courtAllocationConfig.getDesiredWorkloadPerCourt(),
            courtAllocationConfig.getDivorceRatioPerFact(),
            courtAllocationConfig.getSpecificCourtsAllocationPerFact());
    }

    @Bean
    public CourtAllocationConfiguration setUpCourtAllocationConfiguration(
        @Value("${courtAllocationConfigurationJson}") String courtAllocationConfigJson) {
        DocumentContext parsedJson = parseJsonConfiguration(courtAllocationConfigJson);

        Map<String, BigDecimal> desiredWorkloadPerCourt = prepareDesiredWorkloadPerCourt(parsedJson);

        Map<String, BigDecimal> divorceRatioPerFact = prepareDivorceRatioPerFact();

        Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact =
            prepareSpecificCourtsAllocationPerFact(parsedJson);

        return new CourtAllocationConfiguration(desiredWorkloadPerCourt,
            divorceRatioPerFact,
            specificCourtsAllocationPerFact);
    }

    private Map<String, BigDecimal> prepareDesiredWorkloadPerCourt(DocumentContext parsedJson) {
        List<Map> courtsDesiredWorkloadDistribution = parsedJson.read("courtsDesiredWorkloadDistribution");
        return courtsDesiredWorkloadDistribution.stream()
            .flatMap(m -> Stream.of(Pair.of(
                String.valueOf(m.get("courtId")), new BigDecimal(String.valueOf(m.get("percentageOfTotalCases")))
            )))
            .collect(toMap(Pair::getKey, Pair::getValue));
    }

    private Map<String, BigDecimal> prepareDivorceRatioPerFact() {
        Map<String, BigDecimal> divorceRatioPerFact = new HashMap<>();

        divorceRatioPerFact.put(UNREASONABLE_BEHAVIOUR, new BigDecimal("0.30"));
        divorceRatioPerFact.put(SEPARATION_TWO_YEARS, new BigDecimal("0.37"));
        divorceRatioPerFact.put(SEPARATION_FIVE_YEARS, new BigDecimal("0.21"));
        divorceRatioPerFact.put(DESERTION, new BigDecimal("0.11"));
        divorceRatioPerFact.put(ADULTERY, new BigDecimal("0.01"));

        return divorceRatioPerFact;
    }

    private Map<String, Map<String, BigDecimal>> prepareSpecificCourtsAllocationPerFact(DocumentContext parsedJson) {
        Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact;

        List<Map> factSpecificCourtAllocation = parsedJson.read("factSpecificCourtAllocation");
        if (factSpecificCourtAllocation != null) {
            specificCourtsAllocationPerFact = factSpecificCourtAllocation.stream()
                .flatMap(m -> Stream.of(Pair.of(
                    String.valueOf(m.get("fact")), (List<Map>) m.get(SELECTED_COURT_KEY)
                )))
                .collect(toMap(
                    Pair::getKey,
                    pair -> pair.getValue().stream()
                        .collect(toMap(
                            m -> (String) m.get("courtId"),
                            m -> new BigDecimal(String.valueOf(m.get("percentageOfCasesWithThisFactCourtWillHandle")))
                        ))
                ));
        } else {
            specificCourtsAllocationPerFact = new HashMap<>();
        }

        return specificCourtsAllocationPerFact;
    }

    private DocumentContext parseJsonConfiguration(String courtAllocationConfigJson) {
        com.jayway.jsonpath.Configuration parserConfiguration = com.jayway.jsonpath.Configuration.builder()
            .options(Option.DEFAULT_PATH_LEAF_TO_NULL)
            .build();
        return JsonPath.parse(courtAllocationConfigJson, parserConfiguration);
    }

}