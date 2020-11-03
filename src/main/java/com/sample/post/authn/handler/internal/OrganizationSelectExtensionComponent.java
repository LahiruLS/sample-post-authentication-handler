package com.sample.post.authn.handler.internal;

import com.sample.post.authn.handler.impl.OrgSelectPostAuthnHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.identity.application.authentication.framework.handler.request.PostAuthenticationHandler;
import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="com.sample.post.authn.handler.component" immediate="true"
 * @scr.reference name="realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="application.mgt.service"
 * interface="org.wso2.carbon.identity.application.mgt.ApplicationManagementService"
 * cardinality="1..1"
 * policy="dynamic" bind="setApplicationManagementService" unbind="unsetApplicationManagementService"
 */
public class OrganizationSelectExtensionComponent {

    private static final Log log = LogFactory.getLog(OrganizationSelectExtensionComponent.class);

    protected void activate(ComponentContext ctx) {

        try {
            OrgSelectPostAuthnHandler orgSelectPostAuthnHandler = new OrgSelectPostAuthnHandler();
            ctx.getBundleContext().registerService(PostAuthenticationHandler.class.getName(),
                    orgSelectPostAuthnHandler, null);
            log.info("Organization Selection Post Authentication Handler bundle is activated");
            if (log.isDebugEnabled()) {
                log.debug("Organization Selection Post Authentication Handler bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Organization Selection Post Authentication Handler bundle activation Failed", e);
        }
    }

    protected void deactivate(ComponentContext ctx) {

        if (log.isDebugEnabled()) {
            log.debug("Organization Selection Post Authentication Handler bundle is deactivated");
        }
    }

    protected void setRealmService(RealmService realmService) {

        log.debug("Setting the Realm Service");
        OrganizationSelectExtensionServiceHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {

        if (log.isDebugEnabled()) {
            log.debug("Un-Setting the Realm Service");
        }
        OrganizationSelectExtensionServiceHolder.getInstance().setRealmService(null);
    }

    protected void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        if (log.isDebugEnabled()) {
            log.debug("Setting ApplicationManagement Service.");
        }
        OrganizationSelectExtensionServiceHolder.getInstance().
                setApplicationManagementService(applicationManagementService);
    }

    protected void unsetApplicationManagementService(ApplicationManagementService applicationManagementService) {

        if (log.isDebugEnabled()) {
            log.debug("Un-Setting ApplicationManagement Service.");
        }
        OrganizationSelectExtensionServiceHolder.getInstance().setApplicationManagementService(null);
    }
}
