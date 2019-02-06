package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.DefaultCourtAllocator;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocationConfiguration;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

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

        Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact = prepareSpecificCourtsAllocationPerFact(parsedJson);

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

        divorceRatioPerFact.put("unreasonable-behaviour", new BigDecimal("0.30"));
        divorceRatioPerFact.put("separation-2-years", new BigDecimal("0.37"));
        divorceRatioPerFact.put("separation-5-years", new BigDecimal("0.21"));
        divorceRatioPerFact.put("desertion", new BigDecimal("0.11"));
        divorceRatioPerFact.put("adultery", new BigDecimal("0.01"));

        return divorceRatioPerFact;
    }

    private Map<String, Map<String, BigDecimal>> prepareSpecificCourtsAllocationPerFact(DocumentContext parsedJson) {
        Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact;

        List<Map> factSpecificCourtAllocation = parsedJson.read("factSpecificCourtAllocation");
        if (factSpecificCourtAllocation != null) {
            specificCourtsAllocationPerFact = factSpecificCourtAllocation.stream()
                .flatMap(m -> Stream.of(Pair.of(
                    String.valueOf(m.get("fact")), (List<Map>) m.get("courts")
                )))
                .collect(toMap(
                    Pair::getKey,
                    pair -> pair.getValue().stream()
                        .collect(toMap(
                            m -> (String) m.get("courtId"),
                            m -> new BigDecimal(String.valueOf(m.get("percentageOfCasesWithThisFactThisCourtWillHandle")))
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