package io.agora.video;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.SurfaceView;

import java.util.HashMap;

public class GridVideoViewContainer extends RecyclerView {
    private GridVideoViewContainerAdapter mGridVideoViewContainerAdapter;

    public GridVideoViewContainer(Context context) {
        super(context);
    }

    public GridVideoViewContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GridVideoViewContainer(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private boolean initAdapter(Integer localUid, HashMap<Integer, SurfaceView> uids) {
        if (mGridVideoViewContainerAdapter == null) {
            mGridVideoViewContainerAdapter = new GridVideoViewContainerAdapter(getContext(), localUid, uids);
            mGridVideoViewContainerAdapter.setHasStableIds(true);
            return true;
        }
        return false;
    }

    public void initViewContainer(Context context, int localUid, HashMap<Integer, SurfaceView> uids) {
        boolean newCreated = initAdapter(localUid, uids);

        if (!newCreated) {
            mGridVideoViewContainerAdapter.setLocalUid(localUid);
            mGridVideoViewContainerAdapter.init(uids, localUid, true);
        }

        this.setAdapter(mGridVideoViewContainerAdapter);

        int count = uids.size();
        if (count <= 2) {
            this.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        } else if (count > 2 && count <= 4) {
            this.setLayoutManager(new GridLayoutManager(context, 2, RecyclerView.VERTICAL, false));
        }

        mGridVideoViewContainerAdapter.notifyDataSetChanged();
    }

    public SurfaceView getSurfaceView(int index) {
        return mGridVideoViewContainerAdapter.getItem(index).mView;
    }

    public VideoStatusData getItem(int position) {
        return mGridVideoViewContainerAdapter.getItem(position);
    }

    public void notifyDataSetChanged() {
        mGridVideoViewContainerAdapter.notifyDataSetChanged();
    }
}
