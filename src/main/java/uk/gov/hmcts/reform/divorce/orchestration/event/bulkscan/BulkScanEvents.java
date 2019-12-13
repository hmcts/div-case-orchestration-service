package uk.gov.hmcts.reform.divorce.orchestration.event.bulkscan;

public enum BulkScanEvents {

    CREATE("caseCreate");

    private String value;

    BulkScanEvents(String value) {
        this.value = value;
    }

    public String getEventName() {
        return this.value;
    }
}
