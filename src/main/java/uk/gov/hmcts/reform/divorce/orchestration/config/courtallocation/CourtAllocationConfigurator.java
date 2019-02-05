package uk.gov.hmcts.reform.divorce.orchestration.config.courtallocation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CandidateCourtAllocator;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocationConfiguration;
import uk.gov.hmcts.reform.divorce.orchestration.courtallocation.CourtAllocator;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

@Configuration
public class CourtAllocationConfigurator {

    @Autowired
    private CourtAllocationConfiguration courtAllocationConfig;

    private final ObjectMapper objectMapper;

    public CourtAllocationConfigurator(@Autowired ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public CourtAllocationConfiguration setUpEnvironmentCourtAllocationConfiguration(
        @Value("${courtAllocationConfigurationJson}") String courtAllocationConfigJson) {//TODO - refactor
        com.jayway.jsonpath.Configuration parserConfiguration = com.jayway.jsonpath.Configuration.builder().options(Option.DEFAULT_PATH_LEAF_TO_NULL).build();
        DocumentContext parsedJson = JsonPath.parse(courtAllocationConfigJson, parserConfiguration);

        List<Map> courtsDesiredWorkloadDistribution = parsedJson.read("courtsDesiredWorkloadDistribution", List.class);//TODO- i think I could remove class
        Map<String, BigDecimal> desiredWorkloadPerCourt = courtsDesiredWorkloadDistribution.stream()
            .flatMap(m -> Stream.of(Pair.of((String) m.get("courtId"), new BigDecimal(String.valueOf(m.get("percentageOfTotalCases"))))))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));//TODO - shouldn't it be pair?

        List<Map> factSpecificCourtAllocation = Optional.ofNullable((List<Map>) parsedJson.read("factSpecificCourtAllocation"))
            .orElse(emptyList());
        Map<String, Map<String, BigDecimal>> specificCourtsAllocationPerFact = factSpecificCourtAllocation.stream()
            .flatMap(m -> Stream.of(Pair.of((String) m.get("fact"), (List<Map>) m.get("courts"))))
            .collect(Collectors.toMap(Pair::getKey, pair -> pair.getValue().stream().collect(Collectors.toMap(m -> (String) m.get("courtId"), m -> new BigDecimal(String.valueOf(m.get("percentageOfCasesWithThisFactThisCourtWillHandle")))))));

        Map<String, BigDecimal> divorceRatioPerFact = new HashMap<>();//TODO - check if values are accurate according to environment variables in PFE
        divorceRatioPerFact.put("unreasonable-behaviour", new BigDecimal("0.30"));
        divorceRatioPerFact.put("separation-2-years", new BigDecimal("0.37"));
        divorceRatioPerFact.put("separation-5-years", new BigDecimal("0.21"));
        divorceRatioPerFact.put("adultery", new BigDecimal("0.11"));
        divorceRatioPerFact.put("desertion", new BigDecimal("0.01"));//TODO - extract to private method

        return new CourtAllocationConfiguration(desiredWorkloadPerCourt,
            divorceRatioPerFact,
            specificCourtsAllocationPerFact);
    }

    @Bean
    public CourtAllocator configureCourtAllocationFromEnvironmentVariable() {
        return new CandidateCourtAllocator(courtAllocationConfig.getDesiredWorkloadPerCourt(),
            courtAllocationConfig.getDivorceRatioPerFact(),
            courtAllocationConfig.getSpecificCourtsAllocationPerFact());
    }

}