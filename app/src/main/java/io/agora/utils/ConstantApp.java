package io.agora.utils;

import io.agora.rtc.Constants;

public class ConstantApp {
    public static final int MAX_PEER_COUNT = 3;


    //Permission
    public static final int BASE_VALUE_PERMISSION = 0X0001;
    public static final int PERMISSION_REQ_ID_RECORD_AUDIO = BASE_VALUE_PERMISSION + 1;
    public static final int PERMISSION_REQ_ID_CAMERA = BASE_VALUE_PERMISSION + 2;
    public static final int PERMISSION_REQ_ID_WRITE_EXTERNAL_STORAGE = BASE_VALUE_PERMISSION + 3;
    //profiles default 240p
    public static final int DEFAULT_PROFILE_IDX = 2; //
    //MainActivity To LiveRoom
    public static final String MAIN_TO_LIVE_ROOM = "mainAcToliveroomAc";
    public static int[] VIDEO_PROFILES = new int[]{
            Constants.VIDEO_PROFILE_120P,
            Constants.VIDEO_PROFILE_180P,
            Constants.VIDEO_PROFILE_240P,
            Constants.VIDEO_PROFILE_360P,
            Constants.VIDEO_PROFILE_480P,
            Constants.VIDEO_PROFILE_720P};

    public static int[] VIDEO_PROFILES_BITRATE = new int[]{
            65,
            140,
            200,
            400,
            500,
            1130
    };

    public static int[] VIDEO_PROFILES_FRAMERATE = new int[]{
            15,
            15,
            15,
            15,
            15,
            15
    };

    //profiles preference
    public static class PrefManager {
        public static final String PREF_PROPERTY_PROFILE_IDX = "pref_profile_index";
        public static final String PREF_PROPERTY_UID = "pOCXx_uid";
    }
}
