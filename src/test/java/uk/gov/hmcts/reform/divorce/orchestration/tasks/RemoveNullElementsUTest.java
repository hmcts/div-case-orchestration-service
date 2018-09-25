package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(MockitoJUnitRunner.class)
public class RemoveNullElementsUTest {

    @InjectMocks
    private RemoveNullElements  target;

    @Test
    public void givenNullEntry_whenRemoveNullElements_thenReturnNull() {
        assertNull(target.execute(null, null));
    }

    @Test
    public void givenEntryWithNullElements_whenRemoveNullElements_thenReturnEntryWithoutNull() {
        Map<String, Object> input = new HashMap<>();
        input.put("Element", "Not null element");
        input.put("Null Element", null);

        Map<String, Object> expected = Collections.singletonMap("Element","Not null element");
        assertEquals(expected, target.execute(null, input));
    }
}
