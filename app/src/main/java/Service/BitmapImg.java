package Service;

/**
 * Created by potter on 14-12-12.
 */
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

public class BitmapImg {
    /*
    *  不知什么原因, msg.obj 接受 Bitmap 却对 InputStream 有偏见
    * */
    public static Bitmap getBitImgInputStream(InputStream imgInputStream) {
        // System.out.println("In getBitImgInputStream == null? " + (imgInputStream == null));
        if(imgInputStream == null) {
            return null;
        }
        return BitmapFactory.decodeStream(imgInputStream);
    }

}
