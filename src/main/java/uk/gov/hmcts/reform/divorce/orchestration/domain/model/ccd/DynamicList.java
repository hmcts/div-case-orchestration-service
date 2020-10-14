package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

@Value
@Builder
public class DynamicList {
    @JsonProperty("value")
    private ListItem defaultValue;

    @JsonProperty("list_items")
    private List<ListItem> listItems;

    public static DynamicList asDynamicList(List<String> list) {
        List<ListItem> formattedListItems = list.stream()
            .map(DynamicList::toListItem)
            .collect(Collectors.toList());

        return DynamicList.builder().listItems(formattedListItems).build();
    }

    public static ListItem toListItem(String item) {
        return ListItem.builder()
            .code(item)
            .label(item)
            .build();
    }
}
