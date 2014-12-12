package Service;

import android.graphics.Bitmap;
import android.util.Log;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by potter on 14-12-12.
 */
public class Login {
    private static String ROOT_URL = "http://192.168.3.11:7001";
    private static String INDEX_URL = ROOT_URL + "/QDHWSingle/login.jsp";
    private static String CAPTCHA_URL = ROOT_URL + "/QDHWSingle/ValidateCodeServlet?action=ShowValidateCode1";
    //private static String CAPTCHA_URL = "http://jw.qdu.edu.cn/academic/getCaptcha.do?";
    private static String LOGIN_URL = ROOT_URL + "/QDHWSingle/login.do";
    private static String SUCCESS_URL = ROOT_URL + "/QDHWSingle/successqd.jsp";
    private static String LOGOUT_URL = ROOT_URL + "/QDHWSingle/logoff.do";

    public static boolean detectNetwork() {
        return MyHttpClient.getStatus(INDEX_URL);
    }

    public static void get(MyHttpClient http) {
        http.get(INDEX_URL);
    }

    public static String loginPortal(MyHttpClient http, String username, String password, String validatecode) {
        ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("logName", username));
        nvps.add(new BasicNameValuePair("logPW", password));
        nvps.add(new BasicNameValuePair("validatecode", validatecode));
        nvps.add(new BasicNameValuePair("from", "qd"));

        // MyHttpClient webSign = new MyHttpClient();

        String result = null;
        return http.post(LOGIN_URL, nvps);
    }

    public static String logout(MyHttpClient http) {
        return http.get(LOGOUT_URL);
    }

    public static Bitmap getImg (MyHttpClient http) {
        return BitmapImg.getBitImgInputStream(http.getImgInputStream(CAPTCHA_URL));
    }

    public static int loginSuccess(MyHttpClient http, String username, String password, String validatecode) {
        ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("logName", username));
        nvps.add(new BasicNameValuePair("logPW", password));
        nvps.add(new BasicNameValuePair("validatecode", validatecode));
        nvps.add(new BasicNameValuePair("from", "qd"));
        String sourceHtml = http.post(LOGIN_URL, nvps);
        System.out.println("sssssssssssss: " + sourceHtml);

        return LoginStatusRegex.isSuccessLogin(sourceHtml);
    }

    public static int loginSuccess(MyHttpClient http) {
        return LoginStatusRegex.isSuccessLogin(http.get(SUCCESS_URL));
    }
}