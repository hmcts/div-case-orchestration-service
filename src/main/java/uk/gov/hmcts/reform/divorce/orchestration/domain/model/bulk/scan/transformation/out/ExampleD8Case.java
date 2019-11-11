package uk.gov.hmcts.reform.divorce.orchestration.domain.model.bulk.scan.transformation.out;

public class ExampleD8Case {

    public final String firstName;
    public final String lastName;
    public final String bulkScanCaseReference;

    public ExampleD8Case(
        String firstName,
        String lastName,
        String bulkScanCaseReference
    ) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.bulkScanCaseReference = bulkScanCaseReference;
    }
}
