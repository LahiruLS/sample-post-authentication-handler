package com.sample.post.authn.handler.impl;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.util.Base64;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sample.post.authn.handler.PostAuthenticationFailedException;
import com.sample.post.authn.handler.internal.OrganizationSelectExtensionServiceHolder;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.wso2.carbon.identity.application.authentication.framework.config.ConfigurationFacade;
import org.wso2.carbon.identity.application.authentication.framework.config.model.SequenceConfig;
import org.wso2.carbon.identity.application.authentication.framework.config.model.StepConfig;
import org.wso2.carbon.identity.application.authentication.framework.context.AuthenticationContext;
import org.wso2.carbon.identity.application.authentication.framework.exception.FrameworkException;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.impl.DefaultPostAuthenticationHandler;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.authentication.framework.util.FrameworkConstants;
import org.wso2.carbon.identity.application.common.IdentityApplicationManagementException;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.ServiceProvider;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;


public class OrgSelectPostAuthnHandler extends DefaultPostAuthenticationHandler {

    private static final String ORG_PAGE_PROMPTED = "orgPagePrompted";
    private static final String ALLOWED_ORG_LIST = "orgList";
	private static final String LOGIN_ENDPOINT = "login.do";
	private static final String ORGANIZATION_SELECT_ENDPOINT = "organizationSelectForm.jsp";
	public static final String REQUEST_PARAM_SP = "sp";
	public static final String SELECTED_ORGANIZATIONS_PARAM = "organization";
    private static final String ENFORCE_ORGANIZATION_SELECTION = "#ENFORCE_ORG_SELECTION#";

    private static final Log log = LogFactory.getLog(OrgSelectPostAuthnHandler.class);
    private static final int CARBON_SUPER_TENANT = -1234;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                                             AuthenticationContext context) throws FrameworkException {

        //First we need to handler the default post authentication flow
        super.handle(request, response, context);

        //First need to set the post authentication finished to false.
        context.setProperty(FrameworkConstants.POST_AUTHENTICATION_EXTENSION_COMPLETED, false);

        //Now the custom flow begins.
        AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
		String tenantDomain = authenticatedUser.getTenantDomain();

        ServiceProvider serviceProvider = null;
		try {
			serviceProvider = OrganizationSelectExtensionServiceHolder
					.getInstance().getApplicationManagementService().getServiceProvider
                            (context.getServiceProviderName(), tenantDomain);
		} catch (IdentityApplicationManagementException e) {
			String error = "Organization selection Post authentication handler execution failed: Can not find the " +
					"service provider.";
			log.error(error);
			return;
		}
        String description = serviceProvider.getDescription();

        if (log.isDebugEnabled()) {
        	log.debug("sp description : " + description + " username : " +
                    authenticatedUser.getUsernameAsSubjectIdentifier(true,
                            true) + " isFederetedUser  : "
                    + authenticatedUser.isFederatedUser());
		}

        Object object = context.getProperty(FrameworkConstants.POST_AUTHENTICATION_REDIRECTION_TRIGGERED);
        boolean postAuthRequestTriggered = false;
        if (object != null && object instanceof Boolean) {
            postAuthRequestTriggered = (boolean) object;
        }

        if (StringUtils.isNotEmpty(description) && description.contains(ENFORCE_ORGANIZATION_SELECTION)
                && !postAuthRequestTriggered) {
			if (isOrgPagePrompted(context)) {
				handlePostOrganizationSelection(request, response, context);
                context.setProperty(FrameworkConstants.POST_AUTHENTICATION_EXTENSION_COMPLETED, true);
            } else {
				handlePreOrganizationSelection(request, response, context);
			}
		}
    }

    private void handlePreOrganizationSelection(HttpServletRequest request, HttpServletResponse response,
																		AuthenticationContext context)
            throws PostAuthenticationFailedException {

        setAllowedOrgList(context, getUserRoleListAsEncodedParameter(context));
        setOrgPagePromptedState(context);
        redirectToOrganizationSelectionPage(response, context);
    }

    private void handlePostOrganizationSelection(HttpServletRequest request, HttpServletResponse response,
												   AuthenticationContext context) {

		String input = request.getParameter(SELECTED_ORGANIZATIONS_PARAM);
	    if (StringUtils.isNotEmpty(input)) {
            context.setProperty(SELECTED_ORGANIZATIONS_PARAM, input);
            SequenceConfig sequenceConfig = context.getSequenceConfig();
            //TODO: here we can filter the claims as we like. What I am simply do is remove the existing role and add
            // new role user attribute
            ClaimMapping key = ClaimMapping.build("role", "role",
                    null, false);
            //Remove all role
            sequenceConfig.getAuthenticatedUser().getUserAttributes().remove(key);
            //Add only the selected role
            sequenceConfig.getAuthenticatedUser().getUserAttributes().put(key, input);
	    }
    }

    private String getUserRoleListAsEncodedParameter(AuthenticationContext context)
            throws PostAuthenticationFailedException {

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(context);
        RealmService realmService = OrganizationSelectExtensionServiceHolder.getInstance().getRealmService();
        RealmConfiguration realmConfiguration = realmService.getBootstrapRealmConfiguration();
        String encodedString = null;
        byte[] encodedBytes = null;
        try {
            UserRealm userRealm = realmService.getUserRealm(realmConfiguration);
            UserStoreManager userStoreManager = userRealm.getUserStoreManager();
            if (StringUtils.isNotEmpty(authenticatedUser.getUserStoreDomain())) {
                userStoreManager =
                        userStoreManager.getSecondaryUserStoreManager(authenticatedUser.getUserStoreDomain());
            }
            String[] roleList = userStoreManager.getRoleListOfUser(authenticatedUser.getUserName());
            if (ArrayUtils.isNotEmpty(roleList)) {
                for (String role: roleList) {
                    if (encodedString == null) {
                        encodedString = role;
                    } else {
                        encodedString = encodedString.concat(",").concat(role);
                    }
                }
                encodedBytes = Base64.getUrlEncoder().encode(encodedString.getBytes());
            }
            return new String(encodedBytes);
        } catch (UserStoreException e) {
            String error = "Error occurred while handling Post authentication: " + e.getMessage();
            log.error(error, e);
            throw new PostAuthenticationFailedException(error,e);
        }
    }

    private void redirectToOrganizationSelectionPage(HttpServletResponse response, AuthenticationContext context) throws
            PostAuthenticationFailedException {

        URIBuilder uriBuilder;
        try {
            uriBuilder = getUriBuilder(context);
            response.sendRedirect(uriBuilder.build().toString());
        } catch (IOException e) {
            throw new PostAuthenticationFailedException("Error while redirecting to organization selection page.", e);
        } catch (URISyntaxException e) {
            throw new PostAuthenticationFailedException("Error while building redirect URI.", e);
        }
    }

    private URIBuilder getUriBuilder(AuthenticationContext context) throws URISyntaxException {

        AuthenticatedUser authenticatedUser = getUser(context);
        String tenantDomain = authenticatedUser.getTenantDomain();
        
        String organizationSelectEndpointUrl = ConfigurationFacade.getInstance()
                .getAuthenticationEndpointURL().replace(LOGIN_ENDPOINT, ORGANIZATION_SELECT_ENDPOINT);
        log.debug("Organization selection endpoint Url: " + organizationSelectEndpointUrl);
        
        URIBuilder uriBuilder;
        uriBuilder = new URIBuilder(organizationSelectEndpointUrl);
        uriBuilder.addParameter(FrameworkConstants.SESSION_DATA_KEY,
                context.getContextIdentifier());
        uriBuilder.addParameter(REQUEST_PARAM_SP,
                context.getSequenceConfig().getApplicationConfig().getApplicationName());
        uriBuilder.addParameter("username", authenticatedUser.getAuthenticatedSubjectIdentifier());
        uriBuilder.addParameter("applicationName", context.getServiceProviderName());
        uriBuilder.addParameter("tenantDomain", tenantDomain);
        if (StringUtils.isNotEmpty(String.valueOf(context.getParameter(ALLOWED_ORG_LIST)))) {
            uriBuilder.addParameter(ALLOWED_ORG_LIST, String.valueOf(context.getParameter(ALLOWED_ORG_LIST)));
        }
        
        return uriBuilder;
    }

    private AuthenticatedUser getAuthenticatedUser(AuthenticationContext authenticationContext) {

        return authenticationContext.getSequenceConfig().getAuthenticatedUser();
    }

    private void setOrgPagePromptedState(AuthenticationContext authenticationContext) {

        authenticationContext.addParameter(ORG_PAGE_PROMPTED, true);
    }

    private boolean isOrgPagePrompted(AuthenticationContext authenticationContext) {

        return authenticationContext.getParameter(ORG_PAGE_PROMPTED) != null;
    }

    private AuthenticatedUser getUser(AuthenticationContext context) {
        StepConfig stepConfig = context.getSequenceConfig().getStepMap().get(context.getCurrentStep() - 1);
        return stepConfig.getAuthenticatedUser();
    }

    private void setAllowedOrgList(AuthenticationContext authenticationContext, String orgList) {

        authenticationContext.addParameter(ALLOWED_ORG_LIST, orgList);
    }
}
