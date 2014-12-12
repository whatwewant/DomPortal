package Service;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Created by potter on 14-12-12.
 */
public class StoreOrGetData {
    // 保存用户名和密码的文件名
    private static final String FILE_NAME = "data";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Context context;

    public StoreOrGetData(Context context) {
        sharedPreferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public boolean store_username_password(String username, String password, String save) {
        editor.putString("username", username);
        editor.putString("password", password);
        editor.putString("save", save);
        // 确认保存
        editor.commit();
        return true;
    }

    public boolean store_username_password(String save) {
        editor.putString("save", save);
        // 确认保存
        editor.commit();
        return true;
    }

    public boolean store_username_password(HashMap<String, String> data) {
        editor.putString("username", data.get("username"));
        editor.putString("password", data.get("password"));
        editor.putString("save",data.get("save"));
        // 确认保存
        editor.commit();
        return true;
    }

    public HashMap<String, String> get_username_password() {
        HashMap<String, String> data = new HashMap<String, String>();
        if (sharedPreferences == null) {
            return null;
        }

        data.put("username", sharedPreferences.getString("username", null));
        data.put("password", sharedPreferences.getString("password", null));
        data.put("save", sharedPreferences.getString("save", "false"));

        return data;
    }
}
