package mediareader;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class md5Checksum {

    private static byte[] createChecksum(String filename) throws Exception {
        return parseData(new FileInputStream(filename));
    }

    private static byte[] createChecksum(byte[] data) throws Exception {
        return parseData(new ByteArrayInputStream(data));
    }

    private static byte[] parseData(InputStream fis) throws Exception {
        byte[] buffer = new byte[1024];
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                md5.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        return md5.digest();
    }

    public static String getChecksum(String filename) throws Exception {
        return parseChecksum(createChecksum(filename));
    }

    public static String getChecksum(byte[] data) throws Exception {
        return parseChecksum(createChecksum(data));
    }

    private static String parseChecksum(byte[] data) throws Exception {
        String result = "";
        for (int i = 0; i < data.length; i++) {
            result +=
                    Integer.toString((data[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }
}
