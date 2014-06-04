package com.squeed.microgramcaster.upnp;

import org.fourthline.cling.UpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceConfiguration;
import org.fourthline.cling.android.AndroidUpnpServiceImpl;
import org.fourthline.cling.model.types.ServiceType;
import org.fourthline.cling.model.types.UDAServiceType;

public class BrowserUpnpService extends AndroidUpnpServiceImpl {

    @Override
    protected UpnpServiceConfiguration createConfiguration() {
    	
        return new AndroidUpnpServiceConfiguration() {

            @Override
            public int getRegistryMaintenanceIntervalMillis() {
                return 15000;
            }
  
            @Override
            public ServiceType[] getExclusiveServiceTypes() {
                return new ServiceType[]{
                    new UDAServiceType("ContentDirectory") //SwitchPower
                };
            }
        };
    }
}