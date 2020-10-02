package uk.gov.hmcts.reform.divorce.orchestration.testutil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServiceApplicationTestUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<CollectionMember<Document>> getDocumentCollection(Map<String, Object> caseData, String collectionType) {
        return Optional.ofNullable(caseData.get(collectionType))
            .map(i -> objectMapper.convertValue(i, new TypeReference<List<CollectionMember<Document>>>() {}))
            .orElse(new ArrayList<>());
    }

}
