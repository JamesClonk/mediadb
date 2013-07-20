package mediareader.data.gnrl;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import javax.swing.filechooser.FileSystemView;

public class Drive {

    private String id = null;
    private String name = null;
    private String location = null;
    private String uuid = null;
    private String totalSpace = null;
    private String freeSpace = null;
    private String partition = null;
    private String mountPoint = null;
    private String hostId = null;

    public Drive(
            File file,
            Host host) {
        this.setMountPoint(file.getMountPoint());
        this.setUuid(this.getSerialNumber(this.getMountPoint()));
        this.setName(this.getDiskName(file));
        this.setLocation(host.getLocation());
        this.setTotalSpace(String.valueOf(file.getTotalSpace()));
        this.setFreeSpace(String.valueOf(file.getFreeSpace()));
        this.setHostId(host.getId());
    }

    private String getDiskName(File file) {
        // WIN32 solution
        FileSystemView view = FileSystemView.getFileSystemView();
        String diskname = view.getSystemDisplayName(new java.io.File(file.getAbsolutePath().substring(0, 3)));
        if (diskname == null) {
            return null;
        }
        diskname = diskname.trim();
        if (diskname == null || diskname.length() < 1) {
            return null;
        }
        int index = diskname.lastIndexOf(" (");
        if (index > 0) {
            diskname = diskname.substring(0, index);
        }
        view = null;
        return diskname;
    }

    public String getId() {
        return id;
    }

    private String getSerialNumber(String drive) {
        // WIN32 solution
        String result = "";
        try {
            java.io.File file = File.createTempFile("getDriveUUID", ".vbs");
            file.deleteOnExit();
            FileWriter fw = new java.io.FileWriter(file);

            String vbs = "Set objFSO = CreateObject(\"Scripting.FileSystemObject\")\n"
                    + "Set colDrives = objFSO.Drives\n"
                    + "Set objDrive = colDrives.item(\"" + drive + "\")\n"
                    + "Wscript.Echo objDrive.SerialNumber";  // see note
            fw.write(vbs);
            fw.close();
            Process p = Runtime.getRuntime().exec("cscript //NoLogo " + file.getPath());
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                result += line;
            }
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result.trim();
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMountPoint() {
        return mountPoint;
    }

    public void setMountPoint(String mountPoint) {
        this.mountPoint = mountPoint;
    }

    public void setTotalSpace(String totalSpace) {
        this.totalSpace = totalSpace;
    }

    public void setFreeSpace(String freeSpace) {
        this.freeSpace = freeSpace;
    }

    public String getTotalSpace() {
        return totalSpace;
    }

    public String getFreeSpace() {
        return freeSpace;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }
}
