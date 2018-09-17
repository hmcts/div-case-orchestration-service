package uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts;

import java.util.Random;

// Needs to be refactored so we don't have hardcoded content.
// We should have a common library maintaining content like this.
public enum CourtEnum {
    EASTMIDLANDS("East Midlands Regional Divorce Centre", "AA01"),
    WESTMIDLANDS("West Midlands Regional Divorce Centre", "AA02"),
    SOUTHWEST("South West Regional Divorce Centre", "AA03"),
    NORTHWEST("North West Regional Divorce Centre", "AA04");

    private String displayName;
    private String siteId;

    private CourtEnum(String displayName, String siteId) {
        this.displayName = displayName;
        this.siteId = siteId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSiteId() {
        return siteId;
    }
}
