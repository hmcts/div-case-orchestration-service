package uk.gov.hmcts.reform.divorce.support;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNot.not;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_ORDERS;
import static uk.gov.hmcts.reform.divorce.orchestration.testutil.ObjectMapperTestUtil.getJsonFromResourceFile;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeneralOrdersTestHelper {

    private static final String GENERAL_ORDER_ID_ACCESSIBLE_TO_ALL_CITIZENS = "f5734b18-c075-4417-81ce-c0c2e0155dbe";
    private static final String GENERAL_ORDER_ID_ACCESSIBLE_ONLY_TO_CO_RESPONDENT = "87e11e12-073f-442c-94db-63f665f55c42";

    public static Pair<String, Object> getGeneralOrdersToAdd() throws java.io.IOException {
        Map<String, Object> attributesToAdd = getJsonFromResourceFile(
            "/fixtures/general-order/general-orders.json",
            new TypeReference<HashMap<String, Object>>() {
            }
        );
        return ImmutablePair.of(GENERAL_ORDERS, attributesToAdd.get(GENERAL_ORDERS));
    }

    public static void assertGeneralOrdersWereAdequatelyFiltered(String responseJsonData) {
        String generalOrderAccessibleToAllCitizens = getGeneralOrderIdAccessibleToAllCitizens();
        String generalOrderIdAccessibleOnlyToCoRespondent = getGeneralOrderIdAccessibleOnlyToCoRespondent();
        assertThat(responseJsonData, hasJsonPath("$.d8", allOf(
            hasItem(
                hasJsonPath("$.id", is(generalOrderAccessibleToAllCitizens))
            ),
            not(hasItem(
                hasJsonPath("$.id", is(generalOrderIdAccessibleOnlyToCoRespondent))
            ))
        )));
    }

    private static String getGeneralOrderIdAccessibleToAllCitizens() {
        return GENERAL_ORDER_ID_ACCESSIBLE_TO_ALL_CITIZENS;
    }

    private static String getGeneralOrderIdAccessibleOnlyToCoRespondent() {
        return GENERAL_ORDER_ID_ACCESSIBLE_ONLY_TO_CO_RESPONDENT;
    }

}