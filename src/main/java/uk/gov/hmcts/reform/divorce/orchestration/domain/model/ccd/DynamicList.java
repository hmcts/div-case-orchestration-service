package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicList {

    @JsonProperty("value")
    private DynamicListItem value;

    @JsonProperty("list_items")
    private List<DynamicListItem> listItems;

    public DynamicList(@JsonProperty("value") DynamicListItem value, @JsonProperty("list_items") List<DynamicListItem> listItems) {
        this.value = value;
        this.listItems = listItems;
    }
}