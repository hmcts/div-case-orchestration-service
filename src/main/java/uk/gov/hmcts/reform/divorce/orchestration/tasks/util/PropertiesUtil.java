package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import com.google.common.base.Strings;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.function.Predicate.not;

public class PropertiesUtil {

    static String getMandatoryPropertyValueAsStringOrThrowGivenException(Map<String, Object> propertiesMap, String key, Supplier<? extends RuntimeException> exceptionSupplier) {
        Optional<String> property = Optional.ofNullable(propertiesMap.get(key))
            .map(String.class::cast)
            .filter(not(Strings::isNullOrEmpty));

        String valueToReturn;
        if (exceptionSupplier != null) {
            valueToReturn = property.orElseThrow(exceptionSupplier);
        } else {
            valueToReturn = property.orElseThrow();
        }
        return valueToReturn;
    }

}