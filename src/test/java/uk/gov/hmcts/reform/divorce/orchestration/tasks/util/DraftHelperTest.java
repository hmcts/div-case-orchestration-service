package uk.gov.hmcts.reform.divorce.orchestration.tasks.util;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.OrchestrationConstants.IS_DRAFT_KEY;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.DraftHelper.isDraft;

public class DraftHelperTest {

    @Test
    public void isDraftReturnsFalseWhenEmptyMap() {
        assertFalse(isDraft(new HashMap<>()));
    }

    @Test
    public void isDraftReturnsFalseWhenNoFieldFound() {
        assertFalse(isDraft(ImmutableMap.of("other field", true)));
    }

    @Test
    public void isDraftReturnsFalseWhenValueIsFalse() {
        assertFalse(isDraft(ImmutableMap.of(IS_DRAFT_KEY, false)));
    }

    @Test
    public void isDraftReturnsTrueWhenValueIsTrue() {
        assertTrue(isDraft(ImmutableMap.of(IS_DRAFT_KEY, true)));
    }
}
