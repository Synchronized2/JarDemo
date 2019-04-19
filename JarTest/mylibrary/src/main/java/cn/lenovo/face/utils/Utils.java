package cn.lenovo.face.utils;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.lenovo.face.sdk.FaceDetInfo;


/**
 * Created by baohm1 on 2018/3/14.
 */

public class Utils {
    public static List mapToList(final Map map) {
        List list = new ArrayList();
        Iterator iter = map.entrySet().iterator();  //获得map的Iterator
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            list.add(entry.getValue());
        }
        return list;
    }

    public static List mapToList(final Map map, int specifiedKey) {
        List list = new ArrayList();
        list.add(map.get(specifiedKey));
        if (list.isEmpty()) {
            return mapToList(map);
        }
        return list;
    }

    private static final float Diff_Ratio = 0.03f;
    public static boolean UpdateRectIfNeed(FaceDetInfo faceDetInfo, float left, float top, float right, float bottom) {
        RectF orgRectF = faceDetInfo.rectf;
        if (orgRectF == null || orgRectF.width() == 0) {
            faceDetInfo.rectf = new RectF(left, top, right, bottom);
            return true;
        }

        if (isNeedUpdate(orgRectF.left, left)
                || isNeedUpdate(orgRectF.top, top)
                || isNeedUpdate(orgRectF.right, right)
                || isNeedUpdate(orgRectF.bottom, bottom)) {
            faceDetInfo.rectf = new RectF(left, top, right, bottom);
            return true;
        } else {
            return false;
        }
    }

    private static boolean isNeedUpdate(float a, float b) {
        return (Math.abs(a - b) > (Diff_Ratio * a));
    }
}
