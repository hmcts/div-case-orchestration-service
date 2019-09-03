package uk.gov.hmcts.reform.divorce.orchestration.controller.internal;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataMigrationRequest;
import uk.gov.hmcts.reform.divorce.orchestration.event.listener.DataMigrationRequestListener;

import java.time.LocalDate;

import static uk.gov.hmcts.reform.divorce.orchestration.event.domain.DataMigrationRequest.Status.DA;

/**
 * This class provides endpoint so trigger the data migration process on demand (meant to be used for tests).
 */
@RestController
@RequiredArgsConstructor
public class DataMigrationInternalController {

    @Autowired
    private final DataMigrationRequestListener listener;

    @PostMapping(path = "/cases/data-migration/family-man")
    @ApiOperation(value = "Starts data migration for family man for the present day. This is meant to only be used as a testing tool.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Data migration process started"),
        @ApiResponse(code = 400, message = "Bad Request")})
    public void startFamilyManDataMigration() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        listener.onApplicationEvent(new DataMigrationRequest(this, DA, yesterday));
    }
}
