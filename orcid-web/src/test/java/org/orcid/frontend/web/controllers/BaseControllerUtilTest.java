/**
 * =============================================================================
 *
 * ORCID (R) Open Source
 * http://orcid.org
 *
 * Copyright (c) 2012-2014 ORCID, Inc.
 * Licensed under an MIT-Style License (MIT)
 * http://orcid.org/open-source-license
 *
 * This copyright and license information (including a link to the full license)
 * shall be included in its entirety in all copies or substantial portion of
 * the software.
 *
 * =============================================================================
 */
package org.orcid.frontend.web.controllers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.*;
import org.orcid.core.oauth.OrcidProfileUserDetails;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

/**
 * @author rcpeters
 */
public class BaseControllerUtilTest {

    BaseControllerUtil baseControllerUtil = new BaseControllerUtil();

    @Test
    public void getCurrentUserNoSecurittyContext() {
        assertNull(baseControllerUtil.getCurrentUser(null));
    }

    @Test
    public void getCurrentUserNoAuthentication() {
        SecurityContext context = mock(SecurityContext.class);
        assertNull(baseControllerUtil.getCurrentUser(context));
    }
    
    @Test
    public void getCurrentUserWrongAuthenticationClass() {
        SecurityContext context = mock(SecurityContext.class);
        TestingAuthenticationToken testingAuthenticationToken = mock(TestingAuthenticationToken.class);
        when(context.getAuthentication()).thenReturn(testingAuthenticationToken);
        assertNull(baseControllerUtil.getCurrentUser(context));
    }

    @Test
    public void getCurrentUserNoPrincipal() {
        SecurityContext context = mock(SecurityContext.class);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = mock(UsernamePasswordAuthenticationToken.class);
        when(context.getAuthentication()).thenReturn(usernamePasswordAuthenticationToken);
        assertNull(baseControllerUtil.getCurrentUser(context));
    }
    
    @Test
    public void getCurrentUserUsernamePasswordAuthenticationToken() {
        SecurityContext context = mock(SecurityContext.class);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = mock(UsernamePasswordAuthenticationToken.class);
        OrcidProfileUserDetails orcidProfileUserDetails = mock(OrcidProfileUserDetails.class);
        when(context.getAuthentication()).thenReturn(usernamePasswordAuthenticationToken);
        when(usernamePasswordAuthenticationToken.getPrincipal()).thenReturn(orcidProfileUserDetails);
        assertNotNull(baseControllerUtil.getCurrentUser(context));
    }
    
    @Test
    public void getCurrentUserPreAuthenticatedAuthenticationToken() {
        SecurityContext context = mock(SecurityContext.class);
        PreAuthenticatedAuthenticationToken usernamePasswordAuthenticationToken = mock(PreAuthenticatedAuthenticationToken.class);
        OrcidProfileUserDetails orcidProfileUserDetails = mock(OrcidProfileUserDetails.class);
        when(context.getAuthentication()).thenReturn(usernamePasswordAuthenticationToken);
        when(usernamePasswordAuthenticationToken.getPrincipal()).thenReturn(orcidProfileUserDetails);
        assertNotNull(baseControllerUtil.getCurrentUser(context));
    }

}
