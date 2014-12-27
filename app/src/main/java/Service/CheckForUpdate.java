package Service;

import android.content.Context;

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

    public static String VERSION = "1.0.22";
    public static int big = Integer.parseInt(VERSION.replace("\"", "").split("\\.")[0]);
    public static int release = Integer.parseInt(VERSION.replace("\"", "").split("\\.")[1]);
    public static int bug = Integer.parseInt(VERSION.replace("\"", "").split("\\.")[2]);

    private static String newVersion = VERSION;

    public static String get_newest_version() {
        String httpResult = new MyHttpClient().get(VERSION_URL);
        String current_reg = "public static String VERSION = \"(.+)\";";
        String regexResult = LoginStatusRegex.regexHtml(httpResult, current_reg);
        if (regexResult == null)
            return "不存在";
        return regexResult.replaceAll("public static String VERSION = \"", "")
                .replaceAll("\";", "");
    }

    public static String get_newest_version_string() {
        return newVersion;
    }

    public static void set_do_update(Context context) {
        new StoreOrGetData(context).set_do_update(newVersion);
    }

    public static String get_do_update(Context context) {
        return new StoreOrGetData(context).get_do_update();
    }

    public static boolean same_newest_version(Context context) {
        if (get_do_update(context) == null)
            return false;
        return get_do_update(context).equals(newVersion);
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

    public static String checkSilent() {
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
            return null;
        }
        catch (Exception e) {
            return null;
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