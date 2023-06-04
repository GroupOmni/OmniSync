package com.groupomni.omnisync;

public class DeviceListItem {
    private String hostIP;
    private int port;

    public DeviceListItem(String hostIP, int port) {
        this.hostIP = hostIP;
        this.port = port;
    }

    public String getHostIP() {
        return hostIP;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object obj){
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        DeviceListItem other = (DeviceListItem) obj;
        return hostIP.equals(other.getHostIP()) && port == other.getPort();
    }

}
