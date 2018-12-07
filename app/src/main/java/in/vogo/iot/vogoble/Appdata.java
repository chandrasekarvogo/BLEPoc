package in.vogo.iot.vogoble;

public class Appdata {

    String deviceName;
    String timeStamp;
    boolean deviceConnected;
    boolean serviceDiscovered;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean isDeviceConnected() {
        return deviceConnected;
    }

    public void setDeviceConnected(boolean deviceConnected) {
        this.deviceConnected = deviceConnected;
    }

    public boolean isServiceDiscovered() {
        return serviceDiscovered;
    }

    public void setServiceDiscovered(boolean serviceDiscovered) {
        this.serviceDiscovered = serviceDiscovered;
    }
}
