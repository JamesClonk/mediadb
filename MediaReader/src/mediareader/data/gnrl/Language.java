package mediareader.data.gnrl;

public class Language {

    private String id = null;
    private String name = null;
    private String isocode = null;

    public Language(String isocode) {
        // take '???' as default!
        if (isocode == null || isocode.equals("")) {
            isocode = "???";
        }
        this.setIsocode(isocode);
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

    public String getIsocode() {
        return isocode;
    }

    public void setIsocode(String isocode) {
        this.isocode = isocode;
    }
}
