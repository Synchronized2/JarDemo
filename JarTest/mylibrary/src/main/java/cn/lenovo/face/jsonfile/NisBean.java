package cn.lenovo.face.jsonfile;

public class NisBean {


    /**
     * padParam : {"padID":"1","preview_w":1920,"preview_h":1200,"only_max_face":1}
     * cw_face : {"licence":"NDc1OTE0bm9kZXZpY2Vjd2F1dGhvcml6ZfTn5OXk5+Li/+fg5efm4+f74uXk4Obg5Yjm5uvl5ubrkeXm5uvl5uai6+Xm5uvl5uTm6+Xm5uDm1efr5+vn6+er4Ofr5+vn68/n5+Lm5+bi","url_server":"http://10.110.131.61:60001/pad/recognize"}
     * cw_param : {"max_miss_num":5,"min_face_size":150,"quality":0.7,"blur":0.75,"pose":0.5,"light_high":0.9}
     */

    private PadParamBean padParam;
    private CwFaceBean cw_face;
    private CwParamBean cw_param;

    public PadParamBean getPadParam() {
        return padParam;
    }

    public void setPadParam(PadParamBean padParam) {
        this.padParam = padParam;
    }

    public CwFaceBean getCw_face() {
        return cw_face;
    }

    public void setCw_face(CwFaceBean cw_face) {
        this.cw_face = cw_face;
    }

    public CwParamBean getCw_param() {
        return cw_param;
    }

    public void setCw_param(CwParamBean cw_param) {
        this.cw_param = cw_param;
    }

    public static class PadParamBean {
        /**
         * padID : 1
         * preview_w : 1920
         * preview_h : 1200
         * only_max_face : 1
         */

        private String padID;
        private int preview_w;
        private int preview_h;

        public String getPadID() {
            return padID;
        }

        public void setPadID(String padID) {
            this.padID = padID;
        }

        public int getPreview_w() {
            return preview_w;
        }

        public void setPreview_w(int preview_w) {
            this.preview_w = preview_w;
        }

        public int getPreview_h() {
            return preview_h;
        }

        public void setPreview_h(int preview_h) {
            this.preview_h = preview_h;
        }

    }

    public static class CwFaceBean {
        /**
         * licence : NDc1OTE0bm9kZXZpY2Vjd2F1dGhvcml6ZfTn5OXk5+Li/+fg5efm4+f74uXk4Obg5Yjm5uvl5ubrkeXm5uvl5uai6+Xm5uvl5uTm6+Xm5uDm1efr5+vn6+er4Ofr5+vn68/n5+Lm5+bi
         * url_server : http://10.110.131.61:60001/pad/recognize
         */

        private String licence;
        private String url_server;

        public String getLicence() {
            return licence;
        }

        public void setLicence(String licence) {
            this.licence = licence;
        }

        public String getUrl_server() {
            return url_server;
        }

        public void setUrl_server(String url_server) {
            this.url_server = url_server;
        }
    }

    public static class CwParamBean {
        /**
         * max_miss_num : 5
         * min_face_size : 150
         * quality : 0.7
         * blur : 0.75
         * pose : 0.5
         * light_high : 0.9
         */

        private int max_miss_num;
        private int min_face_size;
        private double quality;
        private double blur;
        private double pose;
        private double light_high;
        private double light_low;
        private double similar;

        public int getMax_miss_num() {
            return max_miss_num;
        }

        public void setMax_miss_num(int max_miss_num) {
            this.max_miss_num = max_miss_num;
        }

        public int getMin_face_size() {
            return min_face_size;
        }

        public void setMin_face_size(int min_face_size) {
            this.min_face_size = min_face_size;
        }

        public double getQuality() {
            return quality;
        }

        public void setQuality(double quality) {
            this.quality = quality;
        }

        public double getBlur() {
            return blur;
        }

        public void setBlur(double blur) {
            this.blur = blur;
        }

        public double getPose() {
            return pose;
        }

        public void setPose(double pose) {
            this.pose = pose;
        }

        public double getLight_high() {
            return light_high;
        }

        public void setLight_high(double light_high) {
            this.light_high = light_high;
        }

        public double getLight_low() {
            return light_low;
        }

        public void setLight_low(double light_low) {
            this.light_low = light_low;
        }
        public double getSimilar() {
            return similar;
        }

        public void setSimilar(double similar) {
            this.similar = similar;
        }

    }
}
