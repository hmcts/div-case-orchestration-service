package uk.gov.hmcts.reform.divorce.orchestration.util.elasticsearch;

import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.SearchResult;

import static java.util.Collections.emptyList;

public class CMSElasticSearchUtils {

    public static final SearchResult EMPTY_SEARCH_RESULT = SearchResult.builder().total(0).cases(emptyList()).build();

}