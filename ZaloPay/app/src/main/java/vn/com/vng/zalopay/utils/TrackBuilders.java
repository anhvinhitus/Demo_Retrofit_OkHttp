package vn.com.vng.zalopay.utils;

/**
 * Created by khattn on 1/23/17.
 */

public class TrackBuilders {
    public TrackBuilders() {

    }

    public static class AppTransIdBuilder extends TrackBuilder<TrackBuilders.AppTransIdBuilder> {

        public AppTransIdBuilder() {
            data = new java.util.HashMap<>();
        }

        public AppTransIdBuilder setAppTransId(String s) {
            if(s != null) {
                data.put("apptransid", s);
            }
            return this;
        }

        public AppTransIdBuilder setAppId(int i) {
            if(i != 0) {
                data.put("appid", String.valueOf(i));
            }
            return this;
        }

        public AppTransIdBuilder setStep(int i) {
            if(i != 0) {
                data.put("step", String.valueOf(i));
            }
            return this;
        }

        public AppTransIdBuilder setStepResult(int i) {
            if(i != 0) {
                data.put("step_result", String.valueOf(i));
            }
            return this;
        }

        public AppTransIdBuilder setPcmId(int i) {
            if(i != 0) {
                data.put("pcmid", String.valueOf(i));
            }
            return this;
        }

        public AppTransIdBuilder setTransType(int i) {
            if(i != 0) {
                data.put("transtype", String.valueOf(i));
            }
            return this;
        }

        public AppTransIdBuilder setTransId(long l) {
            if(l != 0) {
                data.put("transid", String.valueOf(l));
            }
            return this;
        }

        public AppTransIdBuilder setSdkResult(int i) {
            if(i != 0) {
                data.put("sdk_result", String.valueOf(i));
            }
            return this;
        }

        public AppTransIdBuilder setServerResult(int i) {
            if(i != 0) {
                data.put("server_result", String.valueOf(i));
            }
            return this;
        }

        public AppTransIdBuilder setSource(java.lang.String s) {
            if(s != null) {
                data.put("source", s);
            }
            return this;
        }
    }

    protected static class TrackBuilder <T extends TrackBuilders.TrackBuilder> {
        java.util.Map<String, String> data;

        public java.util.Map<java.lang.String,java.lang.String> build() {
            return data;
        }
    }
}
