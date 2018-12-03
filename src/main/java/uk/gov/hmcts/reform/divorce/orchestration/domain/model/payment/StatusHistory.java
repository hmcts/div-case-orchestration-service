
package uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "date_created",
    "date_updated",
    "error_code",
    "error_message",
    "external_status",
    "status"
    })
public class StatusHistory {

    @JsonProperty("date_created")
    private String dateCreated;
    @JsonProperty("date_updated")
    private String dateUpdated;
    @JsonProperty("error_code")
    private String errorCode;
    @JsonProperty("error_message")
    private String errorMessage;
    @JsonProperty("external_status")
    private String externalStatus;
    @JsonProperty("status")
    private String status;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("date_created")
    public String getDateCreated() {
        return dateCreated;
    }

    @JsonProperty("date_created")
    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    @JsonProperty("date_updated")
    public String getDateUpdated() {
        return dateUpdated;
    }

    @JsonProperty("date_updated")
    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @JsonProperty("error_code")
    public String getErrorCode() {
        return errorCode;
    }

    @JsonProperty("error_code")
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @JsonProperty("error_message")
    public String getErrorMessage() {
        return errorMessage;
    }

    @JsonProperty("error_message")
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @JsonProperty("external_status")
    public String getExternalStatus() {
        return externalStatus;
    }

    @JsonProperty("external_status")
    public void setExternalStatus(String externalStatus) {
        this.externalStatus = externalStatus;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
