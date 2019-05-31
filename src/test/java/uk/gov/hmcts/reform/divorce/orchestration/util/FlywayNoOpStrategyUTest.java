package uk.gov.hmcts.reform.divorce.orchestration.util;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationState;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FlywayNoOpStrategyUTest {

    @Mock
    private Flyway flywayMock;

    @Mock
    private MigrationInfoService migrationInfoServiceMock;

    @Mock
    private MigrationInfo migrationInfoMock;

    @InjectMocks
    private FlywayNoOpStrategy classToTest;

    @Before
    public void setUp() {
        MigrationInfo[] migrationInfo = new MigrationInfo[1];
        migrationInfo[0] = migrationInfoMock;
        when(flywayMock.info()).thenReturn(migrationInfoServiceMock);
        when(migrationInfoServiceMock.all()).thenReturn(migrationInfo);
    }

    @Test(expected = IllegalStateException.class)
    public void givenMigrationNotApplied_thenThrowException() {
        when(migrationInfoMock.getState()).thenReturn(MigrationState.AVAILABLE);

        classToTest.migrate(flywayMock);
    }

    @Test
    public void givenAllMigration_thenExecuteAsExpected() {
        when(migrationInfoMock.getState()).thenReturn(MigrationState.SUCCESS);

        classToTest.migrate(flywayMock);
    }

}
