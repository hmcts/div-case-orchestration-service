package uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts;

// Needs to be refactored so we don't have hardcoded content.
// We should have a common library maintaining content like this.
public enum CourtEnum {
    EASTMIDLANDS("eastMidlands", "East Midlands Regional Divorce Centre", "AA01"),
    WESTMIDLANDS("westMidlands", "West Midlands Regional Divorce Centre", "AA02"),
    SOUTHWEST("southWest", "South West Regional Divorce Centre", "AA03"),
    NORTHWEST("northWest", "North West Regional Divorce Centre", "AA04");

    private String id;
    private String displayName;
    private String siteId;

    CourtEnum(String id, String displayName, String siteId) {
        this.id = id;
        this.displayName = displayName;
        this.siteId = siteId;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSiteId() {
        return siteId;
    }

}