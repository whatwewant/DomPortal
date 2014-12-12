package Service;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by potter on 14-12-12.
 */

public class LoginStatusRegex {
    public static ArrayList<String> regexHtml(String html, String regex, String type) {
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(html);

        ArrayList<String> listInfo = new ArrayList<String>();

        while(matcher.find()) {
            //System.out.println("In regexHtml: " + matcher.group());
            listInfo.add(matcher.group());
        }

        if (listInfo.isEmpty()) {
            // System.out.println("In regexHtml: 匹配失败");
            return null;
        }
        // System.out.println("In regexHtml: " + listInfo.toString());
        return listInfo;
    }

    public static String regexHtml(String html, String regex) {
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(html);

        while(matcher.find()) {
            System.out.println("In regexHtml: " + matcher.group());
            return matcher.group();
        }

        // System.out.println("In regexHtml: " + listInfo.toString());
        return null;
    }

    public static String loginInfo (String sourceHtml) {
        String regexFailed = "<div class=\"divLoginR4\">(.+)</div>";
        // String regexErrorPassword = "<div class=\"divLoginR4\">(.+)</div>";
        // String regexUsernameDoesNot = "<div class=\"divLoginR4\">(.+)</div>";
        // String regexMaxConnection = "<div class=\"divLoginR4\">(.+)</div>";

        String message = regexHtml(sourceHtml, regexFailed);

        if (message == null)
            return null;

        return message.replaceAll("<div class=\"divLoginR4\">", "").replaceAll("</div>", "");
    }

    public static String logout (String sourceHtml) {
        String regex = "setTimeout\\(\"buttonable\\(\\)\",5000\\);.*alert\\(([^\"]+)\\);";

        String message = "您已经成功下线！需要继续上网请再次登陆"; //regexHtml(sourceHtml, regex);
        System.out.println("Logout: " + sourceHtml.contains(message));

        if (! sourceHtml.contains(message))
            message = "下线失败!";

        message = message.replaceAll("alert\\(\"", "").replaceAll("\"\\)\\;\\n", "");

        return message;
    }

    public static int isSuccessLogin(String htmlSource) {
        //return htmlSource.indexOf("html") > -1;
        // System.out.println("In MyRegex: " + htmlSource);
        // System.out.println("In MyRegex: logoff.do " + htmlSource.indexOf("logoff.do"));
        return htmlSource.indexOf("logoff.do") > -1 ? 0 : -1;
    }
}