package uk.gov.hmcts.reform.divorce.orchestration.util.nfd;

import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@NoArgsConstructor
@Component
public class IdamUsersCsvLoader {

    public List<IdamUser> loadIdamUserList(String fileName) {
        try {
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema csvSchema = csvMapper.typedSchemaFor(IdamUser.class);
            List list = new CsvMapper().readerFor(IdamUser.class)
                .with(csvSchema.withArrayElementSeparator(","))
                .readValues(IdamUsersCsvLoader.class.getClassLoader().getResource(fileName))
                .readAll();

            return list;
        } catch (Exception e) {
            log.error("Error occurred while loading object list from file " + fileName, e);
            return Collections.emptyList();
        }
    }

}
