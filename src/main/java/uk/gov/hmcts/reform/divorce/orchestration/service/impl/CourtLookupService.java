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

    private Map<String, Court> courts;

    public CourtLookupService(@Value("${court.details}") String courtsDetails,
                              @Autowired ObjectMapper objectMapper) throws IOException {
        courts = objectMapper.readValue(courtsDetails,
                new TypeReference<Map<String, Court>>() {
                }
        );
    }

    public Court getCourtByKey(String divorceUnitKey) throws CourtDetailsNotFound {
        if (!courts.containsKey(divorceUnitKey)) {
            throw new CourtDetailsNotFound(divorceUnitKey);
        }

        return courts.get(divorceUnitKey);
    }

}