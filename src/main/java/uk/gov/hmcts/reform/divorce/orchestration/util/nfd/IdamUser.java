package uk.gov.hmcts.reform.divorce.orchestration.util.nfd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.EqualsAndHashCode;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@EqualsAndHashCode
@JsonPropertyOrder(value = {"idamId"})
@JsonRootName("idamUser")
public class IdamUser {

    @JsonProperty
    private String idamId;

    public IdamUser() {
    }

    public IdamUser(String idamId) {
        this.idamId = idamId;
    }

    public String getIdamId() {
        return idamId;
    }

    public void setIdamId(String idamId) {
        this.idamId = idamId;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("IdamUser{");
        sb.append(", idamId=").append(idamId);
        sb.append('}');
        return sb.toString();
    }
}
