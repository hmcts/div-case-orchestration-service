
package uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "cancel",
    "next_url",
    "self"
})
public class Links {

    @JsonProperty("cancel")
    private Cancel cancel;
    @JsonProperty("next_url")
    private NextUrl nextUrl;
    @JsonProperty("self")
    private Self self;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("cancel")
    public Cancel getCancel() {
        return cancel;
    }

    @JsonProperty("cancel")
    public void setCancel(Cancel cancel) {
        this.cancel = cancel;
    }

    @JsonProperty("next_url")
    public NextUrl getNextUrl() {
        return nextUrl;
    }

    @JsonProperty("next_url")
    public void setNextUrl(NextUrl nextUrl) {
        this.nextUrl = nextUrl;
    }

    @JsonProperty("self")
    public Self getSelf() {
        return self;
    }

    @JsonProperty("self")
    public void setSelf(Self self) {
        this.self = self;
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
