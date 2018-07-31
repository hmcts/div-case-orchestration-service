package uk.gov.hmcts.reform.divorce.orchestration.domain.model.divorceapplicationdata;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import uk.gov.hmcts.reform.divorce.orchestration.domain.model.ccd.PaymentCollection;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class DivorceSession {
    @ApiModelProperty(value = "Session expiry timestamp.", hidden = true)
    private long expires;
    @ApiModelProperty(value = "Has petitioners marriage broken down irretrievably?", allowableValues = "Yes, No")
    private String screenHasMarriageBroken;
    @ApiModelProperty(value = "Has petitioner got an address for the respondent?", allowableValues = "Yes, No")
    private String screenHasRespondentAddress;
    @ApiModelProperty(value = "Has petitioner got their marriage certificate?", allowableValues = "Yes, No")
    private String screenHasMarriageCert;
    @ApiModelProperty(value = "Has petitioner got access to a printer?", allowableValues = "Yes, No")
    private String screenHasPrinter;
    @ApiModelProperty(value = "Does petitioner need help with fees?", allowableValues = "Yes, No")
    private String helpWithFeesNeedHelp;
    @ApiModelProperty(value = "Has petitioner applied for Help With Fees?", allowableValues = "Yes, No")
    private String helpWithFeesAppliedForFees;
    @ApiModelProperty(value = "Help with fees reference. Must conform to regex ([Hh][Ww][Ff]-?)?[0-9a-zA-Z]{3}-?[0-9a-zA-Z]{3}$")
    private String helpWithFeesReferenceNumber;
    @ApiModelProperty(value = "Who is petitioner divorcing?", allowableValues = "husband,wife")
    private String divorceWho;
    @ApiModelProperty(value = "Is same sex couple?", allowableValues = "Yes, No")
    private String marriageIsSameSexCouple;
    @ApiModelProperty(value = "The day component of the marriage date in 'dd' format. This field is not currently used.")
    private String marriageDateDay;
    @ApiModelProperty(value = "The month component of the marriage date in 'MM' format. This field is not currently used")
    private String marriageDateMonth;
    @ApiModelProperty(value = "The year component of the marriage date in 'yyyy' format. This field is not currently used")
    private String marriageDateYear;
    @ApiModelProperty(value = "The marriage date in one of the following formats (\"yyyy-MM-dd'T'HH:mm:ss.SSSZ\", \"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\", \"yyyy-MM-dd'T'HH:mm:ss.SSS\", \"EEE, dd MMM yyyy HH:mm:ss zzz\", \"yyyy-MM-dd\")")
    private Date marriageDate;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */ allowableValues = "Yes, No")
    private String marriageCanDivorce;
    @ApiModelProperty(hidden = true) //this field is not mapped to anything
    private String marriageDateIsFuture;
    @ApiModelProperty(hidden = true) //this field is not mapped to anything
    private String marriageDateMoreThan100;
    @ApiModelProperty(value = "Site ID for selected court.")
    private String marriageWhereMarried;
    @ApiModelProperty(value = "Has petitioner got married in the UK?", allowableValues = "Yes, No")
    private String marriedInUk;
    @ApiModelProperty(value = "Is the marriage certificate in English?", allowableValues = "Yes, No")
    private String certificateInEnglish;
    @ApiModelProperty(value = "Has petitioner got a translation of the marriage certificate?", allowableValues = "Yes, No")
    private String certifiedTranslation;
    @ApiModelProperty(value = "Country of marriage.")
    private String countryName;
    @ApiModelProperty(value = "Place of marriage (as on marriage certificate).")
    private String placeOfMarriage;
    @ApiModelProperty(hidden = true) //this field is not mapped to anything
    private List<String> jurisdictionPath;
    @ApiModelProperty(value = "Legal connections.")
    private List<String> jurisdictionConnection;
    @ApiModelProperty(value = "Legal connections content.")
    private Connections connections;
    @ApiModelProperty(value = "Is petitioner resident?", allowableValues = "Yes, No")
    private String jurisdictionPetitionerResidence;
    @ApiModelProperty(value = "Is respondent resident?", allowableValues = "Yes, No")
    private String jurisdictionRespondentResidence;
    @ApiModelProperty(value = "Is petitioner confident with their legal connections?", allowableValues = "Yes, No")
    private String jurisdictionConfidentLegal;
    @ApiModelProperty(hidden = true) //this field is not mapped to anything
    private String jurisdictionConnectionFirst;
    @ApiModelProperty(value = "Last 12 months.", allowableValues = "Yes, No")
    private String jurisdictionLastTwelveMonths;
    @ApiModelProperty(value = "Is petitioner domiciled?", allowableValues = "Yes, No")
    private String jurisdictionPetitionerDomicile;
    @ApiModelProperty(value = "Is respondent domiciled.", allowableValues = "Yes, No")
    private String jurisdictionRespondentDomicile;
    @ApiModelProperty(value = "Is habitually resident in the last six months?", allowableValues = "Yes, No")
    private String jurisdictionLastHabitualResident;
    @ApiModelProperty(value = "Is residual jurisdiction eligible?", allowableValues = "Yes, No")
    private String residualJurisdictionEligible;
    @ApiModelProperty(value = "Connection summary")
    private String connectionSummary;
    @ApiModelProperty(value = "Petitioners contact details to be kept private?", allowableValues = "share, keep")
    private String petitionerContactDetailsConfidential;
    @ApiModelProperty(value = "Petitioner's current first names.")
    private String petitionerFirstName;
    @ApiModelProperty(value = "Petitioner's current last names.")
    private String petitionerLastName;
    @ApiModelProperty(value = "Respondent's current first names.")
    private String respondentFirstName;
    @ApiModelProperty(value = "Respondent's current last names.")
    private String respondentLastName;
    @ApiModelProperty(value = "Petitioner's full name (as on marriage certificate).")
    private String marriagePetitionerName;
    @ApiModelProperty(value = "Respondent's full name (as on marriage certificate).")
    private String marriageRespondentName;
    @ApiModelProperty(value = "Is petitioner's current name the same as that on marriage certificate?", allowableValues = "Yes, No")
    private String petitionerNameDifferentToMarriageCertificate;
    @ApiModelProperty(value = "How did the petitioner change their name?", allowableValues = "marriageCertificate, deedPoll, other")
    private List<String> petitionerNameChangedHow;
    @ApiModelProperty(value = "What other details does the petitioner have of the name change?")
    private String petitionerNameChangedHowOtherDetails;
    @ApiModelProperty(value = "Petitioner's email address?")
    private String petitionerEmail;
    @ApiModelProperty(value = "Petitioner's phone number?")
    private String petitionerPhoneNumber;
    @ApiModelProperty(value = "Petitioner's home address.")
    private Address petitionerHomeAddress;
    @ApiModelProperty(value = "Use petitioners home address as address for service?", allowableValues = "Yes, No")
    private String petitionerCorrespondenceUseHomeAddress;
    @ApiModelProperty(value = "Petitioner's correspondence address.")
    private Address petitionerCorrespondenceAddress;
    @ApiModelProperty(value = "Are petitioner and respondent living together?", allowableValues = "Yes, No")
    private String livingArrangementsLiveTogether;
    @ApiModelProperty(hidden = true) //this field is not described in the CCD_Divorce_V60
    private String livingArrangementsLastLivedTogether;
    @ApiModelProperty(value = "Address petitioner and respondent last lived together.")
    private Address livingArrangementsLastLivedTogetherAddress;
    @ApiModelProperty(value = "Does respondent live at last address?", allowableValues = "Yes, No")
    private String respondentLivesAtLastAddress;
    @ApiModelProperty(value = "Respondent home address.")
    private Address respondentHomeAddress;
    @ApiModelProperty(value = "Use respondent's home address as their address for service?", allowableValues = "Yes, No, Solicitor")
    private String respondentCorrespondenceUseHomeAddress;
    @ApiModelProperty(value = "Respondent service address.")
    private Address respondentCorrespondenceAddress;
    @ApiModelProperty(value = "Fact (reason for divorce)", allowableValues = "unreasonable-behaviour, adultery, separation-2-years, separation-5-years, desertion")
    private String reasonForDivorce;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */ allowableValues = "Yes, No")
    private String reasonForDivorceHasMarriageDate;
    @ApiModelProperty(value = "Is reason for divorce adultery?", allowableValues = "Yes, No")
    private String reasonForDivorceShowAdultery;
    @ApiModelProperty(value = "Is reason for divorce unreasonable behaviour?", allowableValues = "Yes, No")
    private String reasonForDivorceShowUnreasonableBehaviour;
    @ApiModelProperty(value = "Is reason for divorce two year separation?", allowableValues = "Yes, No")
    private String reasonForDivorceShowTwoYearsSeparation;
    @ApiModelProperty(value = "Is reason for divorce five year separation?", allowableValues = "Yes, No")
    private String reasonForDivorceShowFiveYearsSeparation;
    @ApiModelProperty(value = "Is reason for divorce desertion?", allowableValues = "Yes, No")
    private String reasonForDivorceShowDesertion;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */ allowableValues = "Yes, No")
    private String reasonForDivorceLimitReasons;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */ allowableValues = "Yes, No")
    private String reasonForDivorceEnableAdultery;
    @ApiModelProperty(value = "Behaviour details")
    private List<String> reasonForDivorceBehaviourDetails;
    @ApiModelProperty(value = "Does petitioner want to name co-respondent?", allowableValues = "Yes, No")
    private String reasonForDivorceAdulteryWishToName;
    @ApiModelProperty(value = "First name of adultery co-respondent.")
    private String reasonForDivorceAdultery3rdPartyFirstName;
    @ApiModelProperty(value = "Last name of adultery co-respondent.")
    private String reasonForDivorceAdultery3rdPartyLastName;
    @ApiModelProperty(value = "Adultery co-respondent address.")
    private Address reasonForDivorceAdultery3rdAddress;
    @ApiModelProperty(value = "Does petitioner know where the adultery took place?", allowableValues = "Yes, No")
    private String reasonForDivorceAdulteryKnowWhere;
    @ApiModelProperty(value = "Does petitioner know when the adultery took place?", allowableValues = "Yes, No")
    private String reasonForDivorceAdulteryKnowWhen;
    @ApiModelProperty(value = "Adultery details.")
    private String reasonForDivorceAdulteryDetails;
    @ApiModelProperty(value = "When did adultery take place?")
    private String reasonForDivorceAdulteryWhenDetails;
    @ApiModelProperty(value = "Where did adultery take place?")
    private String reasonForDivorceAdulteryWhereDetails;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */ allowableValues = "Yes, No")
    private String reasonForDivorceDesertionAlright;
    @ApiModelProperty(value = "Are there legal proceedings relating to property, marriage or children?", allowableValues = "Yes, No")
    private String legalProceedings;
    @ApiModelProperty(value = "Legal proceedings relating to divorce.", allowableValues = "children, property, marriage")
    private List<String> legalProceedingsRelated;
    @ApiModelProperty(value = "Legal proceeding details")
    private String legalProceedingsDetails;
    @ApiModelProperty(value = "Petitioner want a financial order?", allowableValues = "Yes, No")
    private String financialOrder;
    @ApiModelProperty(value = "Who is financial order for?", allowableValues = "petitioner, children")
    private List<String> financialOrderFor;
    @ApiModelProperty(value = "Petitioner to claim costs?", allowableValues = "Yes, No")
    private String claimsCosts;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */ allowableValues = "Yes, No")
    private String reasonForDivorceAdulteryIsNamed;
    @ApiModelProperty(value = "Claim costs from.", allowableValues = "respondent, co-respondent")
    private List<String> claimsCostsFrom;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */ allowableValues = "Yes, No")
    private String claimsCostsAppliedForFees;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */ allowableValues = "Yes, No")
    private String reasonForDivorceClaiming5YearSeparation;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */ allowableValues = "Yes, No")
    private String reasonForDivorceClaimingAdultery;
    @ApiModelProperty(value = "Separation date day in dd format.")
    private String reasonForDivorceSeperationDay;
    @ApiModelProperty(value = "Separation date month in MM format.")
    private String reasonForDivorceSeperationMonth;
    @ApiModelProperty(value = "Separation date year in yyyy format.")
    private String reasonForDivorceSeperationYear;
    @ApiModelProperty(value = "The separation date in one of the following formats (\"yyyy-MM-dd'T'HH:mm:ss.SSSZ\", \"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\", \"yyyy-MM-dd'T'HH:mm:ss.SSS\", \"EEE, dd MMM yyyy HH:mm:ss zzz\", \"yyyy-MM-dd\").")
    private Date reasonForDivorceSeperationDate;
    @ApiModelProperty(value = "Is separation date same as or after limit date?", allowableValues = "Yes, No")
    private String reasonForDivorceSeperationDateIsSameOrAfterLimitDate;
    @ApiModelProperty(value = "Is separation date in the future?", allowableValues = "Yes, No")
    private String reasonForDivorceSeperationDateInFuture;
    @ApiModelProperty(value = "Is separation date before marriage date?", allowableValues = "Yes, No")
    private String reasonForDivorceSeperationDateBeforeMarriageDate;
    @ApiModelProperty(value = "Desertion date day in dd format.")
    private String reasonForDivorceDesertionDay;
    @ApiModelProperty(value = "Desertion date month in MM format.")
    private String reasonForDivorceDesertionMonth;
    @ApiModelProperty(value = "Desertion date year in yyyy format.")
    private String reasonForDivorceDesertionYear;
    @ApiModelProperty(value = "Is desertion date before marriage?", allowableValues = "Yes, No")
    private String reasonForDivorceDesertionBeforeMarriage;
    @ApiModelProperty(value = "Is desertion date in the future?", allowableValues = "Yes, No")
    private String reasonForDivorceDesertionDateInFuture;
    @ApiModelProperty(value = "Desertion date in one of the following formats (\"yyyy-MM-dd'T'HH:mm:ss.SSSZ\", \"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\", \"yyyy-MM-dd'T'HH:mm:ss.SSS\", \"EEE, dd MMM yyyy HH:mm:ss zzz\", \"yyyy-MM-dd\").")
    private Date reasonForDivorceDesertionDate;
    @ApiModelProperty(value = "Did petitioner agree to the desertion?", allowableValues = "Yes, No")
    private String reasonForDivorceDesertionAgreed;
    @ApiModelProperty(value = "Desertion details.")
    private String reasonForDivorceDesertionDetails;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */ allowableValues = "Yes, No")
    private String marriageIsFuture;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */ allowableValues = "Yes, No")
    private String marriageMoreThan100;
    @ApiModelProperty(hidden = true) // this field is not mapped
    private String respondentHome;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */ allowableValues = "Yes, No")
    private String reasonForDivorceHasMarriage;
    @ApiModelProperty(hidden = true) // this field is not mapped
    private String reasonForDivorceAdultery3rd;
    @ApiModelProperty(hidden = true) // this field is not mapped
    private String reasonForDivorceSeperation;
    @ApiModelProperty(hidden = true) // this field is not mapped
    private String reasonForDivorceSeperationIsSameOrAfterLimit;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */ allowableValues = "Yes, No")
    private String reasonForDivorceSeperationInFuture;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */ allowableValues = "Yes, No")
    private String reasonForDivorceSeperationBeforeMarriage;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */ allowableValues = "Yes, No")
    private String reasonForDivorceDesertionInFuture;
    @ApiModelProperty(/* The spreadsheet does not say what this field means */)
    private String reasonForDivorceDesertion;
    @ApiModelProperty(value = "Respondent current name is the same as that on marriage certificate?", allowableValues = "Yes, No")
    private String respondentNameAsOnMarriageCertificate;
    @ApiModelProperty(value = "Is respondent using a solicitor?", allowableValues = "Yes, No")
    private String respondentCorrespondenceSendToSolicitor;
    @ApiModelProperty(value = "Does petitioner know the respondents home address?", allowableValues = "Yes, No")
    private String respondentKnowsHomeAddress;
    @ApiModelProperty(hidden = true)
    private String sessionKey;
    @ApiModelProperty(value = "Regional divorce unit.")
    private String courts;
    @ApiModelProperty(value = "Name of solicitor used by respondent.")
    private String respondentSolicitorName;
    @ApiModelProperty(value = "Company of solicitor used by respondent.")
    private String respondentSolicitorCompany;
    @ApiModelProperty(value = "Address of solicitor used by respondent.")
    private Address respondentSolicitorAddress;
    @ApiModelProperty(value = "Agree to statement of truth?", allowableValues = "Yes, No")
    private String confirmPrayer;
    @ApiModelProperty(value = "Payment details.")
    private Payment payment;
    @ApiModelProperty(value = "Supporting documentation uploaded with the application")
    private List<UploadedFile> marriageCertificateFiles;
    @ApiModelProperty(value = "Details about existing payments.")
    private List<PaymentCollection> existingPayments;
    @ApiModelProperty(value = "D8 PDF file URL details.")
    @JsonProperty("d8")
    @Setter(AccessLevel.NONE)
    private List<UploadedFile> d8Documents;
    
    public void setD8Documents(List<UploadedFile> d8Documents) {
        d8Documents.forEach(doc->doc.setFileType(UploadedFileType.PETITION));
        this.d8Documents = d8Documents;
    }
}