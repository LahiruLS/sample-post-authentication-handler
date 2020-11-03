package com.sample.post.authn.handler.internal;

import org.wso2.carbon.identity.application.mgt.ApplicationManagementService;
import org.wso2.carbon.user.core.service.RealmService;

public class OrganizationSelectExtensionServiceHolder {

    private static OrganizationSelectExtensionServiceHolder thisInstance =
            new OrganizationSelectExtensionServiceHolder();
    private static ApplicationManagementService applicationManagementService = null;
    private static RealmService realmService;


    private OrganizationSelectExtensionServiceHolder() {

    }

    public static OrganizationSelectExtensionServiceHolder getInstance() {

        if (thisInstance == null)  {
            thisInstance = new OrganizationSelectExtensionServiceHolder();
        }
        return thisInstance;
    }

    public static ApplicationManagementService getApplicationManagementService() {

        if (applicationManagementService == null) {
            throw new IllegalStateException("ApplicationManagementService is not initialized properly");
        }
        return applicationManagementService;
    }

    public void setApplicationManagementService(ApplicationManagementService applicationManagementService) {

        OrganizationSelectExtensionServiceHolder.applicationManagementService = applicationManagementService;
    }

    public static RealmService getRealmService() {

        if (realmService == null) {
            throw new IllegalStateException("RealmService is not initialized properly");
        }
        return realmService;
    }

    public void setRealmService(RealmService realmService) {

        OrganizationSelectExtensionServiceHolder.realmService = realmService;
    }

}
