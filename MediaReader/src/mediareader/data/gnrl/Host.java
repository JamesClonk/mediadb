package mediareader.data.gnrl;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Host {

    private String id = null;
    private String name = null;
    private String location = null;
    private String ip = null;
    private String cpu = null;
    private String ram = null;
    private String os = null;

    public Host(String location) throws UnknownHostException {
        this.setLocation(location);
        
        InetAddress addr = InetAddress.getLocalHost();
        this.setName(addr.getHostName());
        this.setIp(addr.getHostAddress());

        this.setOs(System.getProperty("os.name"));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getCpu() {
        return cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

}
