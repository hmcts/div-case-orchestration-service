package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

@Value
@Builder
public class DynamicList {
    @JsonProperty("value")
    private ListItem value;

    @JsonProperty("list_items")
    private List<ListItem> listItems;

    public static DynamicList asDynamicList(List<String> list) {
        List<ListItem> formattedListItems = list.stream()
            .map(DynamicList::toListItem)
            .collect(Collectors.toList());

        return DynamicList.builder()
            .value(ListItem.builder().build())
            .listItems(formattedListItems)
            .build();
    }

    public static DynamicList asDynamicList(String value) {
        ListItem selectedValue = toListItem(value);
        return DynamicList.builder()
            .value(selectedValue)
            .listItems(asList(selectedValue))
            .build();
    }

    public static ListItem toListItem(String item) {
        return ListItem.builder()
            .code(item)
            .label(item)
            .build();
    }
}
