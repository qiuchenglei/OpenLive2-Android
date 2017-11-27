package io.agora.video;

import android.view.SurfaceView;

public class VideoStatusData {
    public static final int DEFAULT_STATUS = 0;
    public static final int VIDEO_MUTED = 1;
    public static final int AUDIO_MUTED = VIDEO_MUTED << 1;

    public static final int DEFAULT_VOLUME = 0;


    public int mUid;
    public SurfaceView mView;
    public int mStatus;
    public int mVolume;

    public VideoStatusData(int mUid, SurfaceView mView, int mStatus, int mVolume) {
        this.mUid = mUid;
        this.mView = mView;
        this.mStatus = mStatus;
        this.mVolume = mVolume;
    }

    @Override
    public String toString() {
        return "VideoStatusData{" +
                "mUid=" + (mUid & 0xFFFFFFFFL) +
                ", mView=" + mView +
                ", mStatus=" + mStatus +
                ", mVolume=" + mVolume +
                '}';
    }
}
