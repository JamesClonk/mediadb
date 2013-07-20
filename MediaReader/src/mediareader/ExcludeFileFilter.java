package mediareader;

import ch.jamesclonk.Cfg;
import java.io.File;
import java.io.FileFilter;

public class ExcludeFileFilter implements FileFilter {

    private String[] evilFileExtensions = null;

    public ExcludeFileFilter(Cfg cfg) {
        this.evilFileExtensions = cfg.getCfgValue("EXCLUDE_LIST").split(",", -1);
    }

    public boolean accept(File file) {
        for (String extension : this.evilFileExtensions) {
            if (file.getName().toLowerCase().endsWith(extension.toLowerCase())) {
                return false;
            }
        }
        return true;
    }
}
