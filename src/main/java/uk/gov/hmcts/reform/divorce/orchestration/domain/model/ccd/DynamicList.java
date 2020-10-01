package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class DynamicList {
    @JsonProperty("value")
    private ListItem defaultValue;

    @JsonProperty("list_items")
    private List<ListItem> listItems;
}
