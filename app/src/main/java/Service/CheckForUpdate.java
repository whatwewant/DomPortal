package Service;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.smith.cole.domportal.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by potter on 14-12-10.
 */
public class CheckForUpdate {
    private static String APK_URL = "https://github.com/whatwewant/DomPortal/raw/master/app/app-release.apk";
    private static String VERSION_URL = "https://raw.githubusercontent.com/whatwewant/DomPortal/master/app/src/main/java/Service/CheckForUpdate.java";

    public static int big = 1;
    public static int release = 0;
    public static int bug = 2;

    public static String VERSION = "1.0.2";
    private static String newVersion;

    public static String get_newest_version() {
        String httpResult = new MyHttpClient().get(VERSION_URL);
        String current_reg = "public static String VERSION = \"(.+)\";";
        String regexResult = LoginStatusRegex.regexHtml(httpResult, current_reg);
        if (regexResult == null)
            return "不存在";
        return regexResult.replaceAll("public static String VERSION = \"", "")
                .replaceAll("\";", "");
    }

    public static String check() {
        newVersion = get_newest_version();

        try {
            int bigRelease = Integer.parseInt(newVersion.split("\\.")[0]);
            int releaseNum = Integer.parseInt(newVersion.split("\\.")[1]);
            int smallBug = Integer.parseInt(newVersion.split("\\.")[2]);

            if (bigRelease > big ||
                    (bigRelease==big && releaseNum>release) ||
                    (big==bigRelease && release==releaseNum && smallBug>bug)) {
                return ("检测到新版本: Version " + newVersion);
            }
            return "已是最新版本Version: " + VERSION + ", 无需更新";
        }
        catch (Exception e) {
            return "检测失败";
        }
    }

    public static File update() {
        final String fileName = R.string.app_name + newVersion +".apk";
        File tmpFile = new File("/sdcard/update");
        if (!tmpFile.exists()) {
            tmpFile.mkdir();
        }
        final File file = new File("/sdcard/update/" + fileName);

        try {
            URL url = new URL(APK_URL);
            try {
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                InputStream is = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buf = new byte[256];
                conn.connect();
                double count = 0;
                if (conn.getResponseCode() >= 400) {
                    return null;
                } else {
                    while (count <= 100) {
                        if (is != null) {
                            int numRead = is.read(buf);
                            if (numRead <= 0) {
                                break;
                            } else {
                                fos.write(buf, 0, numRead);
                            }

                        } else {
                            break;
                        }

                    }
                }

                conn.disconnect();
                fos.close();
                is.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block

                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        }

        return file;
    }


}