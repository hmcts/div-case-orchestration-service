package uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.payment.PaymentCollection;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode(callSuper = true)
public class CoreCaseData extends AosCaseData {

    @JsonProperty("D8legalProcess")
    private String d8legalProcess;

    @JsonProperty("D8caseReference")
    private String d8caseReference;

    @JsonProperty("createdDate")
    private String createdDate;

    @JsonProperty("D8ScreenHasMarriageBroken")
    private String d8ScreenHasMarriageBroken;

    @JsonProperty("D8ScreenHasRespondentAddress")
    private String d8ScreenHasRespondentAddress;

    @JsonProperty("D8ScreenHasMarriageCert")
    private String d8ScreenHasMarriageCert;

    @JsonProperty("D8ScreenHasPrinter")
    private String d8ScreenHasPrinter;

    @JsonProperty("D8DivorceWho")
    private String d8DivorceWho;

    @JsonProperty("D8MarriageIsSameSexCouple")
    private String d8MarriageIsSameSexCouple;

    @JsonProperty("D8MarriageDate")
    private String d8MarriageDate;

    @JsonProperty("D8MarriedInUk")
    private String d8MarriedInUk;

    @JsonProperty("D8CertificateInEnglish")
    private String d8CertificateInEnglish;

    @JsonProperty("D8CertifiedTranslation")
    private String d8CertifiedTranslation;

    @JsonProperty("D8MarriagePlaceOfMarriage")
    private String d8MarriagePlaceOfMarriage;

    @JsonProperty("D8CountryName")
    private String d8CountryName;

    @JsonProperty("D8RejectMarriageDetails")
    private RejectReason d8RejectMarriageDetails;

    @JsonProperty("D8PetitionerNameDifferentToMarriageCert")
    private String d8PetitionerNameDifferentToMarriageCert;

    @JsonProperty("D8PetitionerEmail")
    private String d8PetitionerEmail;

    @JsonProperty("D8PetitionerPhoneNumber")
    private String d8PetitionerPhoneNumber;

    @JsonProperty("D8PetitionerFirstName")
    private String d8PetitionerFirstName;

    @JsonProperty("D8PetitionerLastName")
    private String d8PetitionerLastName;

    @JsonProperty("D8DerivedPetitionerCurrentFullName")
    private String d8DerivedPetitionerCurrentFullName;

    @JsonProperty("D8PetitionerNameChangedHow")
    private List<String> d8PetitionerNameChangedHow;

    @JsonProperty("D8PetitionerNameChangedHowOtherDetails")
    private String d8PetitionerNameChangedHowOtherDetails;

    @JsonProperty("D8PetitionerContactDetailsConfidential")
    private String d8PetitionerContactDetailsConfidential;

    @JsonProperty("D8PetitionerHomeAddress")
    private Address d8PetitionerHomeAddress;

    @JsonProperty("D8DerivedPetitionerHomeAddress")
    private String d8DerivedPetitionerHomeAddress;

    @JsonProperty("D8PetitionerCorrespondenceAddress")
    private Address d8PetitionerCorrespondenceAddress;

    @JsonProperty("D8DerivedPetitionerCorrespondenceAddr")
    private String d8DerivedPetitionerCorrespondenceAddress;

    @JsonProperty("D8PetitionerCorrespondenceUseHomeAddress")
    private String d8PetitionerCorrespondenceUseHomeAddress;

    @JsonProperty("D8RespondentNameAsOnMarriageCertificate")
    private String d8RespondentNameAsOnMarriageCertificate;

    @JsonProperty("D8RespondentFirstName")
    private String d8RespondentFirstName;

    @JsonProperty("D8RespondentLastName")
    private String d8RespondentLastName;

    @JsonProperty("D8DerivedRespondentCurrentName")
    private String d8DerivedRespondentCurrentName;

    @JsonProperty("D8DerivedRespondentSolicitorDetails")
    private String d8DerivedRespondentSolicitorDetails;

    @JsonProperty("D8RespondentHomeAddress")
    private Address d8RespondentHomeAddress;

    @JsonProperty("D8DerivedRespondentHomeAddress")
    private String d8DerivedRespondentHomeAddress;

    @JsonProperty("D8RespondentCorrespondenceAddress")
    private Address d8RespondentCorrespondenceAddress;

    @JsonProperty("D8DerivedRespondentCorrespondenceAddr")
    private String d8DerivedRespondentCorrespondenceAddr;

    @JsonProperty("D8RespondentSolicitorName")
    private String d8RespondentSolicitorName;

    @JsonProperty("D8RespondentSolicitorCompany")
    private String d8RespondentSolicitorCompany;

    @JsonProperty("respondentSolicitorRepresented")
    private String respondentSolicitorRepresented;

    @JsonProperty("D8RespondentSolicitorAddress")
    private Address d8RespondentSolicitorAddress;

    @JsonProperty("D8RespondentCorrespondenceUseHomeAddress")
    private String d8RespondentCorrespondenceUseHomeAddress;

    @JsonProperty("D8RespondentKnowsHomeAddress")
    private String d8RespondentKnowsHomeAddress;

    @JsonProperty("D8RespondentLivesAtLastAddress")
    private String d8RespondentLivesAtLastAddress;

    @JsonProperty("D8RejectRespondentAddress")
    private RejectReasonAddress d8RejectRespondentAddress;

    @JsonProperty("D8LivingArrangementsTogetherSeparated")
    private String d8LivingArrangementsTogetherSeparated;

    @JsonProperty("D8LivingArrangementsLastLivedTogether")
    private String d8LivingArrangementsLastLivedTogether;

    @JsonProperty("D8LivingArrangementsLiveTogether")
    private String d8LivingArrangementsLiveTogether;

    @JsonProperty("D8LivingArrangementsLastLivedTogethAddr")
    private Address d8LivingArrangementsLastLivedTogethAddr;

    @JsonProperty("D8LegalProceedings")
    private String d8LegalProceedings;

    @JsonProperty("D8LegalProceedingsRelated")
    private List<String> d8LegalProceedingsRelated;

    @JsonProperty("D8LegalProceedingsDetails")
    private String d8LegalProceedingsDetails;

    @JsonProperty("D8ReasonForDivorce")
    private String d8ReasonForDivorce;

    @JsonProperty("D8DerivedStatementOfCase")
    private String d8DerivedStatementOfCase;

    @JsonProperty("D8RejectStatementOfCase")
    private RejectReason d8RejectStatementOfCase;

    @JsonProperty("D8ReasonForDivorceBehaviourDetails")
    private String d8ReasonForDivorceBehaviourDetails;

    @JsonProperty("D8ReasonForDivorceDesertionDate")
    private String d8ReasonForDivorceDesertionDate;

    @JsonProperty("D8ReasonForDivorceDesertionAgreed")
    private String d8ReasonForDivorceDesertionAgreed;

    @JsonProperty("D8ReasonForDivorceDesertionDetails")
    private String d8ReasonForDivorceDesertionDetails;

    @JsonProperty("D8ReasonForDivorceSeperationDate")
    private String d8ReasonForDivorceSeperationDate;

    @JsonProperty("D8ReasonForDivorceAdultery3rdPartyFName")
    private String d8ReasonForDivorceAdultery3rdPartyFName;

    @JsonProperty("D8ReasonForDivorceAdultery3rdPartyLName")
    private String d8ReasonForDivorceAdultery3rdPartyLName;

    @JsonProperty("D8ReasonForDivorceAdulteryDetails")
    private String d8ReasonForDivorceAdulteryDetails;

    @JsonProperty("D8ReasonForDivorceAdulteryKnowWhen")
    private String d8ReasonForDivorceAdulteryKnowWhen;

    @JsonProperty("D8ReasonForDivorceAdulteryWishToName")
    private String d8ReasonForDivorceAdulteryWishToName;

    @JsonProperty("D8ReasonForDivorceAdulteryKnowWhere")
    private String d8ReasonForDivorceAdulteryKnowWhere;

    @JsonProperty("D8ReasonForDivorceAdulteryWhereDetails")
    private String d8ReasonForDivorceAdulteryWhereDetails;

    @JsonProperty("D8ReasonForDivorceAdulteryWhenDetails")
    private String d8ReasonForDivorceAdulteryWhenDetails;

    @JsonProperty("D8ReasonForDivorceAdulteryIsNamed")
    private String d8ReasonForDivorceAdulteryIsNamed;

    @JsonProperty("D8ReasonForDivorceAdultery3rdAddress")
    private Address d8ReasonForDivorceAdultery3rdAddress;

    @JsonProperty("D8FinancialOrder")
    private String d8FinancialOrder;

    @JsonProperty("D8FinancialOrderFor")
    private List<String> d8FinancialOrderFor;

    @JsonProperty("D8HelpWithFeesNeedHelp")
    private String d8HelpWithFeesNeedHelp;

    @JsonProperty("D8HelpWithFeesAppliedForFees")
    private String d8HelpWithFeesAppliedForFees;

    @JsonProperty("D8HelpWithFeesReferenceNumber")
    private String d8HelpWithFeesReferenceNumber;

    @JsonProperty("D8PaymentMethod")
    private String d8PaymentMethod;

    @JsonProperty("D8DivorceCostsClaim")
    private String d8DivorceCostsClaim;

    @JsonProperty("D8DivorceIsNamed")
    private String d8DivorceIsNamed;

    @JsonProperty("D8DivorceClaimFrom")
    private List<String> d8DivorceClaimFrom;

    @JsonProperty("D8JurisdictionConfidentLegal")
    private String d8JurisdictionConfidentLegal;

    @JsonProperty("D8JurisdictionConnection")
    private List<String> d8JurisdictionConnection;

    @JsonProperty("D8JurisdictionLastTwelveMonths")
    private String d8JurisdictionLastTwelveMonths;

    @JsonProperty("D8JurisdictionPetitionerDomicile")
    private String d8JurisdictionPetitionerDomicile;

    @JsonProperty("D8JurisdictionPetitionerResidence")
    private String d8JurisdictionPetitionerResidence;

    @JsonProperty("D8JurisdictionRespondentDomicile")
    private String d8JurisdictionRespondentDomicile;

    @JsonProperty("D8JurisdictionRespondentResidence")
    private String d8JurisdictionRespondentResidence;

    @JsonProperty("D8JurisdictionHabituallyResLast6Months")
    private String d8JurisdictionHabituallyResLast6Months;

    @JsonProperty("D8ResidualJurisdictionEligible")
    private String d8ResidualJurisdictionEligible;

    @JsonProperty("Payments")
    private List<PaymentCollection> payments;

    @JsonProperty("D8DocumentsUploaded")
    private List<CollectionMember<Document>> d8DocumentsUploaded;

    @JsonProperty("D8RejectDocumentsUploaded")
    private List<Document> d8RejectDocumentsUploaded;

    @JsonProperty("D8StatementOfTruth")
    private String d8StatementOfTruth;

    @JsonProperty("D8SolicitorReference")
    private String d8SolicitorReference;

    @JsonProperty("D8DivorceUnit")
    private String d8DivorceUnit;

    @JsonProperty("D8ReasonForDivorceShowAdultery")
    private String d8ReasonForDivorceShowAdultery;

    @JsonProperty("D8ReasonForDivorceShowUnreasonableBehavi")
    private String d8ReasonForDivorceShowUnreasonableBehaviour;

    @JsonProperty("D8ReasonForDivorceShowTwoYearsSeparation")
    private String d8ReasonForDivorceShowTwoYearsSeparation;

    @JsonProperty("D8ReasonForDivorceShowDesertion")
    private String d8ReasonForDivorceShowDesertion;

    @JsonProperty("D8ReasonForDivorceLimitReasons")
    private String d8ReasonForDivorceLimitReasons;

    @JsonProperty("D8ReasonForDivorceEnableAdultery")
    private String d8ReasonForDivorceEnableAdultery;

    @JsonProperty("D8ReasonForDivorceDesertionAlright")
    private String d8ReasonForDivorceDesertionAlright;

    @JsonProperty("D8ClaimsCostsAppliedForFees")
    private String d8ClaimsCostsAppliedForFees;

    @JsonProperty("D8ReasonForDivorceClaimingAdultery")
    private String d8ReasonForDivorceClaimingAdultery;

    @JsonProperty("D8ReasonForDivorceSeperationIsSameOrAftr")
    private String d8ReasonForDivorceSeperationIsSameOrAftr;

    @JsonProperty("D8ReasonForDivorceSeperationInFuture")
    private String d8ReasonForDivorceSeperationInFuture;

    @JsonProperty("D8ReasonForDivorceDesertionInFuture")
    private String d8ReasonForDivorceDesertionInFuture;

    @JsonProperty("D8MarriageCanDivorce")
    private String d8MarriageCanDivorce;

    @JsonProperty("D8MarriageIsFuture")
    private String d8MarriageIsFuture;

    @JsonProperty("D8MarriageMoreThan100")
    private String d8MarriageMoreThan100;

    @JsonProperty("D8MarriagePetitionerName")
    private String d8MarriagePetitionerName;

    @JsonProperty("D8MarriageRespondentName")
    private String d8MarriageRespondentName;

    @JsonProperty("D8ReasonForDivorceDesertionDay")
    private String d8ReasonForDivorceDesertionDay;

    @JsonProperty("D8ReasonForDivorceDesertionMonth")
    private String d8ReasonForDivorceDesertionMonth;

    @JsonProperty("D8ReasonForDivorceDesertionYear")
    private String d8ReasonForDivorceDesertionYear;

    @JsonProperty("D8ReasonForDivorceDesertionBeforeMarriag")
    private String d8ReasonForDivorceDesertionBeforeMarriage;

    @JsonProperty("D8ReasonForDivorceSeperationDay")
    private String d8ReasonForDivorceSeperationDay;

    @JsonProperty("D8ReasonForDivorceSeperationMonth")
    private String d8ReasonForDivorceSeperationMonth;

    @JsonProperty("D8ReasonForDivorceSeperationYear")
    private String d8ReasonForDivorceSeperationYear;

    @JsonProperty("D8DerivedReasonForDivorceAdultery3dPtyNm")
    private String d8DerivedReasonForDivorceAdultery3dPtyNm;

    @JsonProperty("D8DerivedRespondentSolicitorAddr")
    private String d8DerivedRespondentSolicitorAddr;

    @JsonProperty("D8DerivedLivingArrangementsLastLivedAddr")
    private String d8DerivedLivingArrangementsLastLivedAddr;

    @JsonProperty("D8Connections")
    private Connections d8Connections;

    @JsonProperty("D8DocumentsGenerated")
    private List<CollectionMember<Document>> d8Documents;

    @JsonProperty("D8ConnectionSummary")
    private String d8ConnectionSummary;

    @JsonProperty("D8ReasonForDivorceHasMarriage")
    private String d8ReasonForDivorceHasMarriage;

    @JsonProperty("D8ReasonForDivorceShowFiveYearsSeparatio")
    private String d8ReasonForDivorceShowFiveYearsSeparation;

    @JsonProperty("D8ReasonForDivorceClaiming5YearSeparatio")
    private String d8ReasonForDivorceClaiming5YearSeparation;

    @JsonProperty("D8ReasonForDivorceSeperation")
    private String d8ReasonForDivorceSeperation;

    @JsonProperty("D8ReasonForDivorceSeperationBeforeMarria")
    private String d8ReasonForDivorceSeperationBeforeMarriage;

    @JsonProperty("D8ReasonForDivorceDesertion")
    private String d8ReasonForDivorceDesertion;

    @JsonProperty("D8DerivedReasonForDivorceAdultery3rdAddr")
    private String d8DerivedReasonForDivorceAdultery3rdAddr;

    @JsonProperty("D8Cohort")
    private String d8Cohort;

    @JsonProperty("D8InferredPetitionerGender")
    private Gender d8InferredPetitionerGender;

    @JsonProperty("D8InferredRespondentGender")
    private Gender d8InferredRespondentGender;

    @JsonProperty("AosLetterHolderId")
    private String aosLetterHolderId;

    @JsonProperty("solApplicationFeeOrderSummary")
    private OrderSummary orderSummary;

    @JsonProperty("SolicitorFeeAccountNumber")
    private String solicitorFeeAccountNumber;

    @JsonProperty("D8SelectedDivorceCentreSiteId")
    private String d8SelectedDivorceCentreSiteId;

    @JsonProperty("PetitionerSolicitorFirm")
    private String petitionerSolicitorFirm;

    @JsonProperty("PetitionerSolicitorPhone")
    private String petitionerSolicitorPhone;

    @JsonProperty("PetitionerSolicitorName")
    private String petitionerSolicitorName;

    @JsonProperty("DerivedPetitionerSolicitorAddr")
    private String derivedPetitionerSolicitorAddr;

    @JsonProperty("PetitionerSolicitorEmail")
    private String petitionerSolicitorEmail;

    @JsonProperty("SolicitorAgreeToReceiveEmails")
    private String solicitorAgreeToReceiveEmails;

    @JsonProperty("SolStatementOfReconciliationCertify")
    private String solStatementOfReconciliationCertify;

    @JsonProperty("SolStatementOfReconciliationDiscussed")
    private String solStatementOfReconciliationDiscussed;

    @JsonProperty("StatementOfReconciliationComments")
    private String statementOfReconciliationComments;

    @JsonProperty("SolStatementOfReconciliationName")
    private String solStatementOfReconciliationName;

    @JsonProperty("SolStatementOfReconciliationFirm")
    private String solStatementOfReconciliationFirm;

    @JsonProperty("SolPaymentHowToPay")
    private String solPaymentHowToPay;

    @JsonProperty("solSignStatementofTruth")
    private String solSignStatementofTruth;

    @JsonProperty("RespNameDifferentToMarriageCertExplain")
    private String respNameDifferentToMarriageCertExplain;

    @JsonProperty("PetitionerSolicitorToEffectService")
    private String petitionerSolicitorToEffectService;

    @JsonProperty("SolClaimCostsDetails")
    private String solClaimCostsDetails;

    @JsonProperty("SeparationLivedTogetherAsCoupleAgain")
    private String separationLivedTogetherAsCoupleAgain;

    @JsonProperty("SeparationLivedTogetherAsCoupleAgainDetails")
    private String separationLivedTogetherAsCoupleAgainDetails;

    @JsonProperty("D8PetitionerConsent")
    private String d8PetitionerConsent;

}

