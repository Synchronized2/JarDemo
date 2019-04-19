package cn.lenovo.face.jsonfile;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import cn.lenovo.face.Contans;


public class ReadFileUtils {
    private static final String TAG = "ReadFileUtils";

    private static final String JsonConfigFile = "cwFaceConfig.json";
    private static Gson mGson = new Gson();


    public static void updateConfigFromSdcard() {
        Log.d(TAG, "updateConfigFromSdcard: ");
        if (read()) {
            updateParam(Contans.mNisBean);
        }
    }

    public static boolean read(){
        String jsonData = readJsonFile(JsonConfigFile);
        Log.d(TAG,"jsonData:" + jsonData.trim());
        if (!jsonData.isEmpty()) {
            Contans.mNisBean = mGson.fromJson(jsonData.trim(), NisBean.class);
            return true;
        } else {
            return false;
        }
    }

    public static void save(){
        saveFile("cwFaceConfig.json", formatJson(encodeToJson()));
//        saveFile("text1.json", formatJson(encodeToJson()));
    }

    /*
    * 转Bean 为  String
    * */
    private static String encodeToJson(){
        Gson gson = new Gson();
        String jsonStr = gson.toJson(Contans.mNisBean);
        return jsonStr;
    }

    private static void updateParam(NisBean mNisBean) {
        //cw_face
        Contans.sLicence = mNisBean.getCw_face().getLicence();
        Contans.url_server = mNisBean.getCw_face().getUrl_server();
        Contans.ip = Contans.url_server.substring(Contans.url_server.indexOf("//")+2, Contans.url_server.indexOf(":", 10));
        Contans.port = Contans.url_server.substring(Contans.url_server.indexOf(":", 10)+1, Contans.url_server.indexOf("/", Contans.url_server.indexOf(":", 10)));
        //pad param
        Contans.PREVIEW_W = mNisBean.getPadParam().getPreview_w();
        Contans.PREVIEW_H = mNisBean.getPadParam().getPreview_h();
        Contans.PAD_ID = mNisBean.getPadParam().getPadID();

        Contans.MaxLoseNumMiss = mNisBean.getCw_param().getMax_miss_num();
        Contans.MinFaceSize = mNisBean.getCw_param().getMin_face_size();
        Contans.FaceQualityOK = (float) mNisBean.getCw_param().getQuality();
        Contans.FaceBlurOK = (float) mNisBean.getCw_param().getBlur();
        Contans.FacePoseOK = (float) mNisBean.getCw_param().getPose();
        Contans.FaceLightHighThreshold = (float) mNisBean.getCw_param().getLight_high();
        Contans.FaceLightLowThreshold = (float) mNisBean.getCw_param().getLight_low();
        Contans.FaceSimilarThreshold = (float) mNisBean.getCw_param().getSimilar();
    }
    /*
     * 读取json文件
     * */
    private static String readJsonFile(String jsonFile){
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            return "";
        }

        File dataFile = new File(Environment.getExternalStorageDirectory()+ File.separator + "CWModels", jsonFile);
        if (!dataFile.exists()) {
            Log.d (TAG, "/sdcard/"+ jsonFile +" is not exist");
                return "";
        }
        Log.d (TAG, "JsonConfigFile = " + dataFile);

        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileInputStream inputStream = new FileInputStream(dataFile);
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    inputStream));

            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    /*
    * 保存json文件
    * */
    private static void saveFile(String fileName, String content){

        File dataFile = new File(Environment.getExternalStorageDirectory() + File.separator + "CWModels", fileName);
        Log.d (TAG, "dataFile = "+dataFile);

        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            Log.d(TAG, "Cannot use storage.");
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(dataFile, false);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream));

            bw.write(content);
            bw.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
        updateConfigFromSdcard();
    }


    /**
     * 整理json格式
     */
    public static String formatJson(String jsonStr) {
        if (null == jsonStr || "".equals(jsonStr)) return "";
        StringBuilder sb = new StringBuilder();
        char last = '\0';
        char current = '\0';
        int indent = 0;
        for (int i = 0; i < jsonStr.length(); i++) {
            last = current;
            current = jsonStr.charAt(i);
            switch (current) {
                case '{':
                case '[':
                    sb.append(current);
                    sb.append('\n');
                    indent++;
                    addIndentBlank(sb, indent);
                    break;
                case '}':
                case ']':
                    sb.append('\n');
                    indent--;
                    addIndentBlank(sb, indent);
                    sb.append(current);
                    break;
                case ',':
                    sb.append(current);
                    if (last != '\\') {
                        sb.append('\n');
                        addIndentBlank(sb, indent);
                    }
                    break;
                default:
                    sb.append(current);
            }
        }

        return sb.toString();
    }

    /**
     * add space
     * @param sb
     * @param indent
     */
    private static void addIndentBlank(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append('\t');
        }
    }

}
