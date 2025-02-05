package org.orcid.core.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.net.MalformedURLException;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;
import org.orcid.core.cli.anonymize.AnonymizeText;
import org.orcid.jaxb.model.v3.release.common.Contributor;


public class AnonymizeTextTest {
    
    private AnonymizeText anonymizeText = new AnonymizeText();

    @Test
    public void testAnonymizeString() {
        String commonString = "Nonlinear transfer in heated L-mo;des threshold";
        assertNotEquals(commonString, anonymizeText.anonymizeString(commonString));
    }

    @Test
    public void testAnonymizeURL() throws MalformedURLException {
        String url1 = "http://hdl.handle.net/1814/51605";
        assertNotEquals(url1, anonymizeText.anonymizeURL(url1));
        String url2 = "dx.doi.org/10.1093/ajae/aaq063";
        assertNotEquals(url2, anonymizeText.anonymizeURL(url2));
        String url3 = null;
        assertEquals(url3, anonymizeText.anonymizeURL(url3));
        String url4 = "";
        assertEquals(url4, anonymizeText.anonymizeURL(url4));
    }

    @Test
    public void testAnonymizeWorkContributor() throws MalformedURLException, JSONException {
        String contributor1 = "{\"contributorOrcid\":null,\"creditName\":{\"content\":\"Vogel, Peter\"},\"contributorEmail\":null,\"contributorAttributes\":{\"contributorSequence\":\"ADDITIONAL\",\"contributorRole\":\"AUTHOR\"}}";
        JSONObject contrJSON = new JSONObject(contributor1);
        
        Contributor anonymizedContributor1 = anonymizeText.anonymizeWorkContributor(contrJSON);
        assertNotEquals(contrJSON.getJSONObject("creditName").getString("content"), anonymizedContributor1.getCreditName().getContent());

        String contributor2 = "{\"contributorOrcid\":{\"uri\":\"https://sandbox.orcid.org/0000-0002-8811-9027\",\"path\":\"0000-0002-8811-9027\",\"host\":null},\"creditName\":{\"content\":\"Paula Demain\"},\"contributorEmail\":null,\"contributorAttributes\":{\"contributorSequence\":null,\"contributorRole\":\"WRITING_REVIEW_EDITING\"}}";
        contrJSON = new JSONObject(contributor2);
        Contributor anonymizedContributor2 = anonymizeText.anonymizeWorkContributor(contrJSON);
        assertNotEquals(contrJSON.getJSONObject("contributorOrcid").getString("uri"), anonymizedContributor2.getContributorOrcid().getUri());
    }

    @Test
    public void testAnonymizeWorkExternalIdentifier() throws MalformedURLException, JSONException {
        String externalId1 = "{\"relationship\":\"SELF\",\"url\":null,\"workExternalIdentifierType\":\"ISBN\",\"workExternalIdentifierId\":{\"content\":\"978-88-8212-926-2\"}}";
        JSONObject extIdJSON = new JSONObject(externalId1);
        assertNotEquals(extIdJSON.getJSONObject("workExternalIdentifierId").getString("content"), anonymizeText.anonymizeWorkExternalIdentifier(extIdJSON).getValue());
        String externalId2 = "{\"relationship\":\"SELF\",\"url\":{\"value\":\"http://hdl.handle.net/1814/51605\"},\"workExternalIdentifierType\":\"HANDLE\",\"workExternalIdentifierId\":{\"content\":\"http://hdl.handle.net/1814/51605\"}}";
        extIdJSON = new JSONObject(externalId2);
        assertNotEquals(extIdJSON.getJSONObject("url").getString("value"), anonymizeText.anonymizeWorkExternalIdentifier(extIdJSON).getUrl().getValue());
        String externalId3 = "{\"relationship\":\"SELF\",\"url\":{\"value\":\"http://hdl.handle.net/1814/51605\"},\"workExternalIdentifierType\":\"HANDLE\",\"workExternalIdentifierId\":{\"content\":\"http://hdl.handle.net/1814/51605\"}}";
        JSONObject extIdJSONSame = new JSONObject(externalId3);
        assertEquals(anonymizeText.anonymizeWorkExternalIdentifier(extIdJSON).getUrl().getValue(), anonymizeText.anonymizeWorkExternalIdentifier(extIdJSONSame).getUrl().getValue());

    }

}
