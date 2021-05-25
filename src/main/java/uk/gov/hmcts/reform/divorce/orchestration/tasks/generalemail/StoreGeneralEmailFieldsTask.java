package uk.gov.hmcts.reform.divorce.orchestration.tasks.generalemail;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.divorce.model.ccd.CollectionMember;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.GeneralEmailDetails;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.Task;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskContext;
import uk.gov.hmcts.reform.divorce.orchestration.framework.workflow.task.TaskException;
import uk.gov.hmcts.reform.divorce.orchestration.util.AuthUtil;
import uk.gov.hmcts.reform.idam.client.IdamClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_DETAILS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_DETAILS_COLLECTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_OTHER_RECIPIENT_NAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.CcdFields.GENERAL_EMAIL_PARTIES;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getAuthToken;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getMandatoryPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.orchestration.tasks.util.TaskUtils.getOptionalPropertyValueAsString;
import static uk.gov.hmcts.reform.divorce.utils.DateUtils.formatDateTimeForCcd;

@Component
@RequiredArgsConstructor
public class StoreGeneralEmailFieldsTask implements Task<Map<String, Object>> {

    private final IdamClient idamClient;
    private final AuthUtil authUtil;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> execute(TaskContext context, Map<String, Object> payload) throws TaskException {
        Map<String, Object> payloadToReturn = new HashMap<>(payload);

        GeneralEmailDetails generalEmailDetails = GeneralEmailDetails.builder()
            .generalEmailDateTime(formatDateTimeForCcd(LocalDateTime.now()))
            .generalEmailParties(getMandatoryPropertyValueAsString(payload, GENERAL_EMAIL_PARTIES))
            .generalEmailOtherRecipientEmail(getOptionalPropertyValueAsString(payload, GENERAL_EMAIL_OTHER_RECIPIENT_EMAIL, null))
            .generalEmailOtherRecipientName(getOptionalPropertyValueAsString(payload, GENERAL_EMAIL_OTHER_RECIPIENT_NAME, null))
            .generalEmailCreatedBy(getUserName(getAuthToken(context)))
            .generalEmailBody(getMandatoryPropertyValueAsString(payload, GENERAL_EMAIL_DETAILS))
            .build();

        payloadToReturn.put(GENERAL_EMAIL_DETAILS_COLLECTION, addGeneralEmailDetailsToTheCollection(payload, generalEmailDetails));

        return payloadToReturn;
    }

    private List<CollectionMember<GeneralEmailDetails>> addGeneralEmailDetailsToTheCollection(Map<String, Object> payload,
                                                                                              GeneralEmailDetails generalEmailDetails) {
        List<CollectionMember<GeneralEmailDetails>> generalEmailDetailsCollection = Optional.ofNullable(payload.get(GENERAL_EMAIL_DETAILS_COLLECTION))
            .map(generalEmailDetailsCollectionObject -> objectMapper.convertValue(generalEmailDetailsCollectionObject,
                new TypeReference<List<CollectionMember<GeneralEmailDetails>>>() {}))
            .orElse(new ArrayList<>());

        generalEmailDetailsCollection.add(CollectionMember.buildCollectionMember(generalEmailDetails));

        return generalEmailDetailsCollection;
    }

    private String getUserName(String authToken) {
        return idamClient
            .getUserDetails(authUtil.getBearerToken(authToken))
            .getFullName();
    }
}
