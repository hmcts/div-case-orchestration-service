package uk.gov.hmcts.reform.divorce.orchestration.csv.extractor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DaCsvExtractor implements CsvExtractor {

    public static final String CASE_REFERENCE_NUMBER = "iCaseReferenceNumber";
    public static final String DA_APPLICATION_DATE = "DAApplicationDate";
    public static final String DN_PRONOUNCEMENT_DATE = "DNPronouncementDate";
    public static final String PARTY_APPLYING_FOR_DA = "PartyApplyingForDA";
    public static final String NEW_LINE = "\n";

    private String filePath = "uk.gov.hmcts.reform.divorce.orchestration.csv.extractor.csvFile";
    private String headers;


    public DaCsvExtractor() {
        headers = String.format(
                "%s, %s, %s, %s",
                CASE_REFERENCE_NUMBER,
                DA_APPLICATION_DATE,
                DN_PRONOUNCEMENT_DATE,
                PARTY_APPLYING_FOR_DA
        );
    }

    @Override
    public BufferedWriter produceCvsFile(Set<Map<String, String>> data) throws IOException {
        BufferedWriter csvFile = new BufferedWriter(new FileWriter(createFile()));
        csvFile.write(headers);
        csvFile.append(NEW_LINE);

        Iterator<Map<String, String>> dataMap = data.iterator();

        while (dataMap.hasNext()) {
            Map<String, String> csvRow = dataMap.next();
            String formattedCsvRow = formatCsvRow(csvRow);
            csvFile.append(formattedCsvRow);
            csvFile.append(NEW_LINE);
        }

        csvFile.flush();
        csvFile.close();

        return csvFile;
    }

    private File createFile() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        return new File("DA_" + dateFormat.format(date) + ".csv");
    }


    private String formatCsvRow(Map<String, String> csvRow) {
        return  String.format(
                "%s, %s, %s, %s",
                csvRow.get(CASE_REFERENCE_NUMBER),
                csvRow.get(DA_APPLICATION_DATE),
                csvRow.get(DN_PRONOUNCEMENT_DATE),
                csvRow.get(PARTY_APPLYING_FOR_DA));
    }

}
