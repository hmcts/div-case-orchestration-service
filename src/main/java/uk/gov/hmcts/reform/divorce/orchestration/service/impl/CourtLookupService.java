package uk.gov.hmcts.reform.divorce.orchestration.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.courts.Court;
import uk.gov.hmcts.reform.divorce.orchestration.exception.CourtDetailsNotFound;

import java.io.IOException;
import java.util.Map;

@Service
public class CourtLookupService {

    private final Map<String, Court> courts;

    public CourtLookupService(@Value("${court.details}") String courtsDetails,
                              @Autowired ObjectMapper objectMapper) throws IOException {
        courts = objectMapper.readValue(courtsDetails,
            new TypeReference<Map<String, Court>>() {
            }
        );

        //Add courtId to Court object - this can be refactored when story DIV-4239 is played
        for (Map.Entry<String, Court> courtEntry : courts.entrySet()) {
            String courtId = courtEntry.getKey();
            courtEntry.getValue().setCourtId(courtId);
        }
    }

    public Court getCourtByKey(String divorceUnitKey) throws CourtDetailsNotFound {
        if (!courts.containsKey(divorceUnitKey)) {
            throw new CourtDetailsNotFound(divorceUnitKey);
        }

        return courts.get(divorceUnitKey);
    }

}