package uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template;

import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.DocumentGenerationInfo;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact;

import java.util.Map;
import java.util.Optional;

import static lombok.AccessLevel.PRIVATE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_RESPONDENT_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_ADULTERY_RESPONDENT_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_FIVE_YEAR_SEPARATION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_FIVE_YEAR_SEPARATION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_DOCUMENT_TYPE;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.AOSPackOfflineConstants.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_FILENAME;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OFFLINE_ADULTERY_RESPONDENT;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OFFLINE_FIVE_YEAR_SEPARATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OFFLINE_TWO_YEAR_SEPARATION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.document.template.DocumentType.AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.ADULTERY;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.DESERTION;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_FIVE_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.SEPARATION_TWO_YEARS;
import static uk.gov.hmcts.reform.divorce.orchestration.domain.model.facts.DivorceFact.UNREASONABLE_BEHAVIOUR;

@NoArgsConstructor(access = PRIVATE)
public class AosPackOfflineDocuments {

    private static final Map<DivorceFact, DocumentGenerationInfo> documentGenerationInfoPerDivorceFact = Map.of(
        SEPARATION_TWO_YEARS, new DocumentGenerationInfo(
            AOS_OFFLINE_TWO_YEAR_SEPARATION,
            AOS_OFFLINE_TWO_YEAR_SEPARATION_DOCUMENT_TYPE,
            AOS_OFFLINE_TWO_YEAR_SEPARATION_FILENAME
        ),
        SEPARATION_FIVE_YEARS, new DocumentGenerationInfo(
            AOS_OFFLINE_FIVE_YEAR_SEPARATION,
            AOS_OFFLINE_FIVE_YEAR_SEPARATION_DOCUMENT_TYPE,
            AOS_OFFLINE_FIVE_YEAR_SEPARATION_FILENAME
        ),
        ADULTERY, new DocumentGenerationInfo(
            AOS_OFFLINE_ADULTERY_RESPONDENT,
            AOS_OFFLINE_ADULTERY_RESPONDENT_DOCUMENT_TYPE,
            AOS_OFFLINE_ADULTERY_RESPONDENT_FILENAME
        ),
        UNREASONABLE_BEHAVIOUR, new DocumentGenerationInfo(
            AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION,
            AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_DOCUMENT_TYPE,
            AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_FILENAME
        ),
        DESERTION, new DocumentGenerationInfo(
            AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION,
            AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_DOCUMENT_TYPE,
            AOS_OFFLINE_UNREASONABLE_BEHAVIOUR_AND_DESERTION_FILENAME
        )
    );

    public static Optional<DocumentGenerationInfo> getDocumentGenerationInfoByDivorceFact(DivorceFact divorceFact) {
        return Optional.ofNullable(documentGenerationInfoPerDivorceFact.get(divorceFact));
    }

}