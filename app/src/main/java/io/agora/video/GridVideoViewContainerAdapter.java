package io.agora.video;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import io.agora.R;
import io.agora.utils.ConstantApp;


public class GridVideoViewContainerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected final LayoutInflater mInflater;
    protected final Context mContext;
    protected int mItemWidth;
    protected int mItemHeight;
    private ArrayList<VideoStatusData> mUsers;
    private int mLocalUid;

    public GridVideoViewContainerAdapter(Context context, int localUid, HashMap<Integer, SurfaceView> uids) {
        mContext = context;
        mInflater = ((Activity) context).getLayoutInflater();

        mUsers = new ArrayList<>();

        init(uids, localUid, false);
    }

    public int getLocalUid() {
        return mLocalUid;
    }

    public void setLocalUid(int uid) {
        mLocalUid = uid;
    }

    public void init(HashMap<Integer, SurfaceView> uids, int localUid, boolean force) {
        for (HashMap.Entry<Integer, SurfaceView> entry : uids.entrySet()) {
            if (entry.getKey() == 0 || entry.getKey() == mLocalUid) {
                boolean found = false;
                for (VideoStatusData status : mUsers) {
                    if ((status.mUid == entry.getKey() && status.mUid == 0) || status.mUid == mLocalUid) { // first time
                        status.mUid = mLocalUid;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    mUsers.add(0, new VideoStatusData(mLocalUid, entry.getValue(), VideoStatusData.DEFAULT_STATUS, VideoStatusData.DEFAULT_VOLUME));
                }
            } else {
                boolean found = false;
                for (VideoStatusData status : mUsers) {
                    if (status.mUid == entry.getKey()) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    mUsers.add(new VideoStatusData(entry.getKey(), entry.getValue(), VideoStatusData.DEFAULT_STATUS, VideoStatusData.DEFAULT_VOLUME));
                }
            }
        }

        Iterator<VideoStatusData> it = mUsers.iterator();
        while (it.hasNext()) {
            VideoStatusData status = it.next();

            if (uids.get(status.mUid) == null) {
                Log.e("gvca-->", "after_changed remove not exited members " + (status.mUid & 0xFFFFFFFFL) + " " + status.mView);
                it.remove();
            }
        }

        if (force || mItemWidth == 0 || mItemHeight == 0) {
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics outMetrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(outMetrics);

            int count = uids.size();
            int DividerX = 1;
            int DividerY = 1;
            if (count == 2) {
                DividerY = 2;
            } else if (count >= 3) {
                DividerX = 2;
                DividerY = 2;
            }

            mItemWidth = outMetrics.widthPixels / DividerX;
            mItemHeight = outMetrics.heightPixels / DividerY;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.video_view_container, parent, false);
        v.getLayoutParams().width = mItemWidth;
        v.getLayoutParams().height = mItemHeight;
        return new VideoUserStatusHolder(v);
    }

    protected final void stripSurfaceView(SurfaceView view) {
        ViewParent parent = view.getParent();
        if (parent != null) {
            ((FrameLayout) parent).removeView(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        VideoUserStatusHolder myHolder = ((VideoUserStatusHolder) holder);

        final VideoStatusData user = mUsers.get(position);

        Log.e("onBindViewHolder ", position + " " + user + " " + myHolder + " " + myHolder.itemView);

        FrameLayout holderView = (FrameLayout) myHolder.itemView;

        if (holderView.getChildCount() == 0) {
            SurfaceView target = user.mView;
            stripSurfaceView(target);
            holderView.addView(target, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    @Override
    public int getItemCount() {
        int sizeLimit = mUsers.size();
        if (sizeLimit >= ConstantApp.MAX_PEER_COUNT + 1) {
            sizeLimit = ConstantApp.MAX_PEER_COUNT + 1;
        }
        return sizeLimit;
    }

    public VideoStatusData getItem(int position) {
        return mUsers.get(position);
    }

    @Override
    public long getItemId(int position) {
        VideoStatusData user = mUsers.get(position);

        SurfaceView view = user.mView;
        if (view == null) {
            throw new NullPointerException("SurfaceView destroyed for user " + user.mUid + " " + user.mStatus + " " + user.mVolume);
        }

        return (String.valueOf(user.mUid) + System.identityHashCode(view)).hashCode();
    }
}
