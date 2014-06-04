package com.squeed.microgramcaster.upnp;

import org.fourthline.cling.model.meta.Device;
import org.fourthline.cling.model.meta.Service;

public class DeviceDisplay {
	Device device;

    public DeviceDisplay(Device device) {
        this.device = device;
    }

    public Device getDevice() {
        return device;
    }

    // DOC:DETAILS
    public String getDetailsMessage() {
        StringBuilder sb = new StringBuilder();
        if (getDevice().isFullyHydrated()) {
            sb.append(getDevice().getDisplayString());
            sb.append("\n\n");
            for (Service service : getDevice().getServices()) {
                sb.append(service.getServiceType()).append("\n");
            }
        } else {
            //sb.append(getString(R.string.deviceDetailsNotYetAvailable));
        	sb.append("Device details not yet available");
        }
        return sb.toString();
    }
    // DOC:DETAILS

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceDisplay that = (DeviceDisplay) o;
        return device.equals(that.device);
    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }

    @Override
    public String toString() {
        String name =
            getDevice().getDetails() != null && getDevice().getDetails().getFriendlyName() != null
                ? getDevice().getDetails().getFriendlyName()
                : getDevice().getDisplayString();
        // Display a little star while the device is being loaded (see performance optimization earlier)
        return device.isFullyHydrated() ? name : name + " *";
    }
}
