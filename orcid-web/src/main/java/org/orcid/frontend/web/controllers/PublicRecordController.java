package org.orcid.frontend.web.controllers;

import org.orcid.core.exception.DeactivatedException;
import org.orcid.core.exception.LockedException;
import org.orcid.core.exception.OrcidDeprecatedException;
import org.orcid.core.exception.OrcidNoResultException;
import org.orcid.core.exception.OrcidNotClaimedException;
import org.orcid.core.groupIds.issn.IssnPortalUrlBuilder;
import org.orcid.core.manager.OrgDisambiguatedManager;
import org.orcid.core.manager.ProfileEntityCacheManager;
import org.orcid.core.manager.v3.ActivityManager;
import org.orcid.core.manager.v3.MembersManager;
import org.orcid.core.manager.v3.read_only.AddressManagerReadOnly;
import org.orcid.core.manager.v3.read_only.AffiliationsManagerReadOnly;
import org.orcid.core.manager.v3.read_only.EmailManagerReadOnly;
import org.orcid.core.manager.v3.read_only.ExternalIdentifierManagerReadOnly;
import org.orcid.core.manager.v3.read_only.GroupIdRecordManagerReadOnly;
import org.orcid.core.manager.v3.read_only.PeerReviewManagerReadOnly;
import org.orcid.core.manager.v3.read_only.PersonalDetailsManagerReadOnly;
import org.orcid.core.manager.v3.read_only.ProfileFundingManagerReadOnly;
import org.orcid.core.manager.v3.read_only.ProfileKeywordManagerReadOnly;
import org.orcid.core.manager.v3.read_only.RecordManagerReadOnly;
import org.orcid.core.manager.v3.read_only.ResearchResourceManagerReadOnly;
import org.orcid.core.manager.v3.read_only.ResearcherUrlManagerReadOnly;
import org.orcid.core.manager.v3.read_only.WorkManagerReadOnly;
import org.orcid.core.oauth.OrcidOauth2TokenDetailService;
import org.orcid.core.togglz.Features;
import org.orcid.core.utils.v3.SourceUtils;
import org.orcid.frontend.web.pagination.ResearchResourcePaginator;
import org.orcid.frontend.web.pagination.WorksPaginator;
import org.orcid.frontend.web.util.LanguagesMap;
import org.orcid.jaxb.model.message.OrcidType;
import org.orcid.jaxb.model.v3.release.record.Addresses;
import org.orcid.jaxb.model.v3.release.record.Biography;
import org.orcid.jaxb.model.v3.release.record.Email;
import org.orcid.jaxb.model.v3.release.record.Emails;
import org.orcid.jaxb.model.v3.release.record.Keywords;
import org.orcid.jaxb.model.v3.release.record.Name;
import org.orcid.jaxb.model.v3.release.record.OtherName;
import org.orcid.jaxb.model.v3.release.record.OtherNames;
import org.orcid.jaxb.model.v3.release.record.Person;
import org.orcid.jaxb.model.v3.release.record.PersonExternalIdentifiers;
import org.orcid.jaxb.model.v3.release.record.PersonalDetails;
import org.orcid.jaxb.model.v3.release.record.Record;
import org.orcid.jaxb.model.v3.release.record.ResearcherUrls;
import org.orcid.jaxb.model.v3.release.record.summary.ActivitiesSummary;
import org.orcid.jaxb.model.v3.release.record.summary.AffiliationGroup;
import org.orcid.jaxb.model.v3.release.record.summary.DistinctionSummary;
import org.orcid.jaxb.model.v3.release.record.summary.EmploymentSummary;
import org.orcid.jaxb.model.v3.release.record.summary.FundingGroup;
import org.orcid.jaxb.model.v3.release.record.summary.Fundings;
import org.orcid.jaxb.model.v3.release.record.summary.InvitedPositionSummary;
import org.orcid.jaxb.model.v3.release.record.summary.MembershipSummary;
import org.orcid.jaxb.model.v3.release.record.summary.ServiceSummary;
import org.orcid.jaxb.model.v3.release.record.summary.WorkGroup;
import org.orcid.jaxb.model.v3.release.record.summary.Works;
import org.orcid.persistence.jpa.entities.ProfileEntity;
import org.orcid.pojo.PeerReviewMinimizedSummary;
import org.orcid.pojo.PublicRecord;
import org.orcid.pojo.ajaxForm.AddressForm;
import org.orcid.pojo.ajaxForm.AddressesForm;
import org.orcid.pojo.ajaxForm.BiographyForm;
import org.orcid.pojo.ajaxForm.ExternalIdentifiersForm;
import org.orcid.pojo.ajaxForm.KeywordsForm;
import org.orcid.pojo.ajaxForm.NamesForm;
import org.orcid.pojo.ajaxForm.OtherNamesForm;
import org.orcid.pojo.ajaxForm.PojoUtil;
import org.orcid.pojo.ajaxForm.WebsitesForm;
import org.orcid.pojo.summary.AffiliationSummary;
import org.orcid.pojo.summary.ExternalIdentifiersSummary;
import org.orcid.pojo.summary.RecordSummary;
import org.orcid.utils.DateUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class PublicRecordController extends BaseWorkspaceController {

    @Resource(name = "membersManagerV3")
    MembersManager membersManager;

    @Resource(name = "workManagerReadOnlyV3")
    private WorkManagerReadOnly workManagerReadOnly;

    @Resource(name = "peerReviewManagerReadOnlyV3")
    private PeerReviewManagerReadOnly peerReviewManagerReadOnly;

    @Resource(name = "profileFundingManagerReadOnlyV3")
    private ProfileFundingManagerReadOnly profileFundingManagerReadOnly;

    @Resource
    private WorksPaginator worksPaginator;

    @Resource(name = "activityManagerV3")
    private ActivityManager activityManager;

    @Resource(name = "languagesMap")
    private LanguagesMap lm;

    @Resource
    private ProfileEntityCacheManager profileEntityCacheManager;

    @Resource(name = "groupIdRecordManagerReadOnlyV3")
    private GroupIdRecordManagerReadOnly groupIdRecordManagerReadOnly;

    @Resource(name = "personalDetailsManagerReadOnlyV3")
    private PersonalDetailsManagerReadOnly personalDetailsManagerReadOnly;

    @Resource
    private OrgDisambiguatedManager orgDisambiguatedManager;

    @Resource
    private OrcidOauth2TokenDetailService orcidOauth2TokenService;

    @Resource(name = "sourceUtilsV3")
    private SourceUtils sourceUtils;

    @Resource(name = "affiliationsManagerReadOnlyV3")
    private AffiliationsManagerReadOnly affiliationsManagerReadOnly;

    @Resource
    private ResearchResourcePaginator researchResourcePaginator;

    @Resource(name = "researchResourceManagerReadOnlyV3")
    private ResearchResourceManagerReadOnly researchResourceManagerReadOnly;

    @Resource
    private IssnPortalUrlBuilder issnPortalUrlBuilder;

    @Resource(name = "emailManagerReadOnlyV3")
    protected EmailManagerReadOnly emailManagerReadOnly;

    @Resource(name = "addressManagerReadOnlyV3")
    private AddressManagerReadOnly addressManagerReadOnly;

    @Resource(name = "profileKeywordManagerReadOnlyV3")
    private ProfileKeywordManagerReadOnly keywordManagerReadOnly;

    @Resource(name = "researcherUrlManagerReadOnlyV3")
    private ResearcherUrlManagerReadOnly researcherUrlManagerReadOnly;

    @Resource(name = "externalIdentifierManagerReadOnlyV3")
    private ExternalIdentifierManagerReadOnly externalIdentifierManagerReadOnly;

    @Resource(name = "recordManagerReadOnlyV3")
    private RecordManagerReadOnly recordManagerReadOnly;

    public static int ORCID_HASH_LENGTH = 8;
    private static final String PAGE_SIZE_DEFAULT = "50";

    @RequestMapping(value = "/{orcid:(?:\\d{4}-){3,}\\d{3}[\\dX]}/public-record.json", method = RequestMethod.GET)
    public @ResponseBody
    PublicRecord getPublicRecord(@PathVariable("orcid") String orcid) {
        PublicRecord publicRecord = new PublicRecord();
        Boolean isDeprecated = false;

        try {
            // Check if the profile is deprecated or locked
            orcidSecurityManager.checkProfile(orcid);
        } catch (LockedException | DeactivatedException e) {
            publicRecord.setDisplayName(localeManager.resolveMessage("public_profile.deactivated.given_names") + " "
                    + localeManager.resolveMessage("public_profile.deactivated.family_name"));
            return publicRecord;
        } catch (OrcidNotClaimedException e) {
            publicRecord.setDisplayName(localeManager.resolveMessage("orcid.reserved_for_claim"));
            return publicRecord;
        } catch (OrcidDeprecatedException e) {
            isDeprecated = true;
        } catch (OrcidNoResultException e) {
            return publicRecord; 
        }

        publicRecord = getRecord(orcid);
        if (isDeprecated) {
            // If deprecated be sure to remove all fields
            publicRecord.setBiography(null);
            publicRecord.setOtherNames(null);
            publicRecord.setCountries(null);
            publicRecord.setKeyword(null);
            publicRecord.setEmails(null);
            publicRecord.setExternalIdentifier(null);
        }

        // If the id belongs to a group the name field is removed
        ProfileEntity profile = profileEntityCacheManager.retrieve(orcid);
        if (OrcidType.GROUP.name().equals(profile.getOrcidType())) {
            publicRecord.setDisplayName(null);
        }

        return publicRecord;
    }

    public @ResponseBody
    PublicRecord getRecord(String orcid) {
        PublicRecord publicRecord = new PublicRecord();

        PersonalDetails publicPersonalDetails = personalDetailsManagerReadOnly.getPublicPersonalDetails(orcid);
        // Fill personal details
        if (publicPersonalDetails != null) {
            // Get display name
            String displayName = "";

            if (publicPersonalDetails.getName() != null) {
                Name name = publicPersonalDetails.getName();
                if (name.getVisibility().equals(org.orcid.jaxb.model.v3.release.common.Visibility.PUBLIC)) {
                    if (name.getCreditName() != null && !PojoUtil.isEmpty(name.getCreditName().getContent())) {
                        displayName = name.getCreditName().getContent();
                    } else {
                        if (name.getGivenNames() != null && !PojoUtil.isEmpty(name.getGivenNames().getContent())) {
                            displayName = name.getGivenNames().getContent() + " ";
                        }
                        if (name.getFamilyName() != null && !PojoUtil.isEmpty(name.getFamilyName().getContent())) {
                            displayName += name.getFamilyName().getContent();
                        }
                    }
                    publicRecord.setNames(NamesForm.valueOf(name));
                }
            }

            if (!PojoUtil.isEmpty(displayName)) {
                // <Published Name> (<ORCID iD>) - ORCID | Connecting Research
                // and Researchers
                publicRecord.setTitle(displayName + " (" + orcid + ") - " + getMessage("layout.public-layout.title"));
                publicRecord.setDisplayName(displayName);
            }

            // Get biography
            if (publicPersonalDetails.getBiography() != null) {
                Biography bio = publicPersonalDetails.getBiography();
                if (org.orcid.jaxb.model.v3.release.common.Visibility.PUBLIC.equals(bio.getVisibility()) && !PojoUtil.isEmpty(bio.getContent())) {
                    publicRecord.setBiography(BiographyForm.valueOf(bio));
                }
            }

            // Fill other names
            OtherNames publicOtherNames = publicPersonalDetails.getOtherNames();
            if (publicOtherNames != null && publicOtherNames.getOtherNames() != null) {
                Iterator<OtherName> it = publicOtherNames.getOtherNames().iterator();
                while (it.hasNext()) {
                    OtherName otherName = it.next();
                    if (!org.orcid.jaxb.model.v3.release.common.Visibility.PUBLIC.equals(otherName.getVisibility())) {
                        it.remove();
                    }
                }
            }
            publicRecord.setOtherNames(OtherNamesForm.valueOf(publicOtherNames));
        }

        // Fill country
        Addresses publicAddresses;
        publicAddresses = addressManagerReadOnly.getPublicAddresses(orcid);
        if (publicAddresses != null && publicAddresses.getAddress() != null) {
            AddressesForm form = AddressesForm.valueOf(publicAddresses);
            // Set country name
            if (form != null && form.getAddresses() != null) {
                Map<String, String> countries = retrieveIsoCountries();
                for (AddressForm addressForm : form.getAddresses()) {
                    addressForm.setCountryName(countries.get(addressForm.getIso2Country().getValue().name()));
                }
            }
            publicRecord.setCountries(form);
        }

        // Fill keywords
        Keywords publicKeywords;
        publicKeywords = keywordManagerReadOnly.getPublicKeywords(orcid);
        publicRecord.setKeyword(KeywordsForm.valueOf(publicKeywords));

        // Fill researcher urls
        ResearcherUrls publicResearcherUrls;
        publicResearcherUrls = researcherUrlManagerReadOnly.getPublicResearcherUrls(orcid);

        publicRecord.setWebsite(WebsitesForm.valueOf(publicResearcherUrls));

        // Fill emails
        Emails publicEmails;
        publicEmails = emailManagerReadOnly.getPublicEmails(orcid);

        Emails filteredEmails = new Emails();
        if (Features.HIDE_UNVERIFIED_EMAILS.isActive()) {
            filteredEmails.setEmails(new ArrayList<Email>(publicEmails.getEmails().stream().filter(e -> e.isVerified()).collect(Collectors.toList())));
        } else {
            filteredEmails.setEmails(new ArrayList<Email>(publicEmails.getEmails()));
        }

        publicRecord.setEmails(org.orcid.pojo.ajaxForm.Emails.valueOf(filteredEmails));

        // Fill external identifiers
        PersonExternalIdentifiers publicPersonExternalIdentifiers;
        publicPersonExternalIdentifiers = externalIdentifierManagerReadOnly.getPublicExternalIdentifiers(orcid);

        publicRecord.setExternalIdentifier(ExternalIdentifiersForm.valueOf(publicPersonExternalIdentifiers));

        Long lastModifiedTime = getLastModifiedTime(orcid);

        publicRecord.setLastModifiedTime(new java.util.Date(lastModifiedTime));

        return publicRecord;
    }

    @RequestMapping(value = "/{orcid:(?:\\d{4}-){3,}\\d{3}[\\dX]}/summary.json", method = RequestMethod.GET)
    public @ResponseBody
    RecordSummary getSummaryRecord(@PathVariable("orcid") String orcid) {
        RecordSummary recordSummary = new RecordSummary();
        Boolean isDeprecated = false;

        try {
            // Check if the profile is deprecated or locked
            orcidSecurityManager.checkProfile(orcid);
        } catch (LockedException | DeactivatedException e) {
            if (e instanceof LockedException) {
                recordSummary.setStatus("locked");
            } else {
                recordSummary.setStatus("deactivated");
            }
            recordSummary.setName(localeManager.resolveMessage("public_profile.deactivated.given_names") + " "
                    + localeManager.resolveMessage("public_profile.deactivated.family_name"));
            return recordSummary;
        } catch (OrcidNotClaimedException e) {
            recordSummary.setName(localeManager.resolveMessage("orcid.reserved_for_claim"));
            return recordSummary;
        } catch (OrcidDeprecatedException e) {
            isDeprecated = true;
        } catch (OrcidNoResultException e) {
            return recordSummary;
        }

        if (isDeprecated) {
            recordSummary.setStatus("deprecated");
            recordSummary.setEmploymentAffiliations(null);
            recordSummary.setProfessionalActivities(null);
            recordSummary.setExternalIdentifiers(null);
        } else {
            recordSummary = getSummary(orcid);
            recordSummary.setStatus("active");
        }

        return recordSummary;
    }

    public @ResponseBody
    RecordSummary getSummary(String orcid) {
        RecordSummary recordSummary = new RecordSummary();

        Record record = recordManagerReadOnly.getPublicRecord(orcid, false);
        Person person = record.getPerson();
        if (person != null) {
            String displayName = "undefined";
            Name name = person.getName();
            if (name != null) {
                if (name.getVisibility().equals(org.orcid.jaxb.model.v3.release.common.Visibility.PUBLIC)) {
                    if (name.getCreditName() != null && !PojoUtil.isEmpty(name.getCreditName().getContent())) {
                        displayName = name.getCreditName().getContent();
                    } else {
                        if (name.getGivenNames() != null && !PojoUtil.isEmpty(name.getGivenNames().getContent())) {
                            displayName = name.getGivenNames().getContent() + " ";
                        }
                        if (name.getFamilyName() != null && !PojoUtil.isEmpty(name.getFamilyName().getContent())) {
                            displayName += name.getFamilyName().getContent();
                        }
                    }
                }
            }
            recordSummary.setName(displayName);
        }

        ActivitiesSummary activitiesSummary = record.getActivitiesSummary();
        Collection<AffiliationGroup<EmploymentSummary>> employmentsSummary = activitiesSummary.getEmployments().getEmploymentGroups();

        List<AffiliationSummary> employmentAffiliations = new ArrayList<>();

        if (employmentsSummary.size() > 0) {
            Stream<AffiliationGroup<EmploymentSummary>> employmentsList = employmentsSummary.stream().limit(3);
            employmentsList.forEach(e -> {
                for (EmploymentSummary s : e.getActivities()) {
                    employmentAffiliations.add(AffiliationSummary.valueOf(s, orcid, "employment"));
                }
            });
        }

        recordSummary.setEmploymentAffiliationsCount(employmentsSummary.size());
        recordSummary.setEmploymentAffiliations(employmentAffiliations);

        List<AffiliationSummary> professionalActivities = retrieveProfessionalActivities(activitiesSummary, orcid);

        if (professionalActivities.size() > 3) {
            recordSummary.setProfessionalActivities(Arrays.asList(professionalActivities.get(0), professionalActivities.get(1), professionalActivities.get(2)));
        } else {
            recordSummary.setProfessionalActivities(professionalActivities);
        }

        recordSummary.setProfessionalActivitiesCount(professionalActivities.size());

        PersonExternalIdentifiers personExternalIdentifiers;

        personExternalIdentifiers = externalIdentifierManagerReadOnly.getPublicExternalIdentifiers(orcid);

        recordSummary.setExternalIdentifiers(ExternalIdentifiersSummary.valueOf(personExternalIdentifiers, orcid));

        Works works = activitiesSummary.getWorks();

        List<WorkGroup> workGroups = works.getWorkGroup();

        AtomicInteger validatedWorks = new AtomicInteger();
        AtomicInteger selfAssertedWorks = new AtomicInteger();

        if (workGroups != null) {
            workGroups.forEach(work -> {
                work.getWorkSummary().forEach(w -> {
                    if (w.getSource().getSourceClientId() != null && !orcid.equals(w.getSource().getSourceClientId())) {
                        validatedWorks.getAndIncrement();
                    } else {
                        selfAssertedWorks.getAndIncrement();
                    }
                });
            });
        }

        recordSummary.setSelfAssertedWorks(selfAssertedWorks.get());
        recordSummary.setValidatedWorks(validatedWorks.get());

        Fundings fundings = activitiesSummary.getFundings();

        List<FundingGroup> fundingGroups = fundings.getFundingGroup();

        AtomicInteger validatedFunds = new AtomicInteger();
        AtomicInteger selfAssertedFunds = new AtomicInteger();

        if (fundingGroups != null) {
            fundingGroups.forEach(fundingGroup -> {
                fundingGroup.getFundingSummary().forEach(funding -> {
                    if (funding.getSource().getSourceClientId() != null && !orcid.equals(funding.getSource().getSourceClientId())) {
                        validatedFunds.getAndIncrement();
                    } else {
                        selfAssertedFunds.getAndIncrement();
                    }
                });
            });
        }

        recordSummary.setSelfAssertedFunds(selfAssertedFunds.get());
        recordSummary.setValidatedFunds(validatedFunds.get());

        List<PeerReviewMinimizedSummary> peerReviewMinimizedSummaryList = peerReviewManagerReadOnly.getPeerReviewMinimizedSummaryList(orcid, true);

        AtomicInteger publicationGrants = new AtomicInteger();

        if (peerReviewMinimizedSummaryList != null) {
            peerReviewMinimizedSummaryList.forEach(peerReviewMinimizedSummary -> {
                publicationGrants.set(publicationGrants.intValue() + peerReviewMinimizedSummary.getPutCodes().size());
            });
            recordSummary.setPeerReviewsTotal(peerReviewMinimizedSummaryList.size());
            recordSummary.setPeerReviewPublicationGrants(publicationGrants.intValue());
        } else {
            recordSummary.setPeerReviewsTotal(0);
            recordSummary.setPeerReviewPublicationGrants(0);
        }

        ProfileEntity profileEntity = profileEntityManager.findByOrcid(orcid);

        recordSummary.setLastModified(formatDate(record.getHistory().getLastModifiedDate().getValue()));
        recordSummary.setCreation(formatDate(DateUtils.convertToXMLGregorianCalendar(profileEntity.getDateCreated())));

        recordSummary.setOrcid(recordManagerReadOnly.getOrcidIdentifier(orcid).getUri());

        return recordSummary;
    }

    private List<AffiliationSummary> retrieveProfessionalActivities(ActivitiesSummary activitiesSummary, String orcid) {
        List<AffiliationSummary> professionalActivities = new ArrayList<>();

        Collection<AffiliationGroup<MembershipSummary>> membershipSummary = activitiesSummary.getMemberships().getMembershipGroups();
        Collection<AffiliationGroup<ServiceSummary>> serviceSummary = activitiesSummary.getServices().getServiceGroups();
        Collection<AffiliationGroup<InvitedPositionSummary>> invitedPositionSummary = activitiesSummary.getInvitedPositions().getInvitedPositionGroups();
        Collection<AffiliationGroup<DistinctionSummary>> distinctionSummary = activitiesSummary.getDistinctions().getDistinctionGroups();

        membershipSummary.forEach(e -> {
            for (MembershipSummary m : e.getActivities()) {
                professionalActivities.add(AffiliationSummary.valueOf(m, orcid, "membership"));
            }
        });
        serviceSummary.forEach(e -> {
            for (ServiceSummary m : e.getActivities()) {
                professionalActivities.add(AffiliationSummary.valueOf(m, orcid, "service"));
            }
        });
        invitedPositionSummary.forEach(e -> {
            for (InvitedPositionSummary m : e.getActivities()) {
                professionalActivities.add(AffiliationSummary.valueOf(m, orcid, "invited-position"));
            }
        });
        distinctionSummary.forEach(e -> {
            for (DistinctionSummary m : e.getActivities()) {
                professionalActivities.add(AffiliationSummary.valueOf(m, orcid, "distinction"));
            }
        });
        return professionalActivities;
    }

    private String formatDate(XMLGregorianCalendar xmlGregorianCalendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        GregorianCalendar gc = xmlGregorianCalendar.toGregorianCalendar();
        return sdf.format(gc.getTime());
    }

    private Long getLastModifiedTime(String orcid) {
        return profileEntityManager.getLastModified(orcid);
    }
}
