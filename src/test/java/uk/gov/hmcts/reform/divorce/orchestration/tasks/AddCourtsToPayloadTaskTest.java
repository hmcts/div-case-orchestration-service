package uk.gov.hmcts.reform.divorce.orchestration.tasks;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.service.impl.CourtLookupService;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;
import static org.junit.rules.ExpectedException.none;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AddCourtsToPayloadTaskTest {

    //"court" is, unfortunately, the name that is already used in the Divorce session format in many places.
    //Changing it now would probably be more trouble than it's worth
    private static final String COURTS_JSON_KEY = "court";

    @Rule
    public ExpectedException expectedException = none();

    @Mock
    private CourtLookupService courtLookupService;

    @InjectMocks
    private AddCourtsToPayloadTask classUnderTest;

    private static final Map MOCKED_ALL_COURTS_JSON = singletonMap("allCourts", emptyMap());

    @Before
    public void setUp() {
        when(courtLookupService.getAllCourts()).thenReturn(MOCKED_ALL_COURTS_JSON);
    }

    @Test
    public void shouldReturnCourtsInPayload() throws TaskException {
        Map<String, Object> returnedPayload = classUnderTest.execute(null, singletonMap("incomingKey", "incomingValue"));

        assertThat(returnedPayload, hasEntry("incomingKey", "incomingValue"));
        assertThat(returnedPayload, hasEntry(COURTS_JSON_KEY, MOCKED_ALL_COURTS_JSON));
    }

    @Test
    public void shouldOverwriteExistingCourtList() throws TaskException {
        Map<String, Object> returnedPayload = classUnderTest.execute(null, singletonMap(COURTS_JSON_KEY, "{'preExisting':'courtList'}"));

        assertThat(returnedPayload, hasEntry(COURTS_JSON_KEY, MOCKED_ALL_COURTS_JSON));
    }

}