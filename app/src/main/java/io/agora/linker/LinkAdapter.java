package io.agora.linker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import io.agora.R;

public class LinkAdapter extends BaseAdapter {
    private List<Integer> mUids;
    private LayoutInflater mInflater;
    private LinkListener mListener;

    public LinkAdapter(WeakReference<Context> context, List<Integer> uids, @NonNull LinkListener linkListener) {
        if (context.get() != null) {
            mInflater = LayoutInflater.from(context.get());
            mUids = uids;
            mListener = linkListener;
        }
    }

    public void addLinkers(int uid) {
        if (!mUids.contains(uid))
            mUids.add(new Integer(uid));
        notifyDataSetChanged();
    }

    public void removeLinkers(int uid) {
        mUids.remove(new Integer(uid));
        if (mUids.size() == 0) {
            if (mListener != null) {
                mListener.onListEmptyNotify();
            }
        }
        notifyDataSetChanged();
    }

    public List<Integer> getAllLinkers() {
        return mUids;
    }

    @Override
    public int getCount() {
        return mUids.size();
    }

    @Override
    public Object getItem(int i) {
        return mUids.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        final Holder mHolder;
        if (view == null) {
            mHolder = new Holder();
            view = mInflater.inflate(R.layout.link_item, null);
            mHolder.mTvUid = view.findViewById(R.id.tv_link_uid);
            mHolder.mBtnConfirm = view.findViewById(R.id.btn_link_confirm);
            mHolder.mBtnCancel = view.findViewById(R.id.btn_link_cancel);
            view.setTag(mHolder);
        } else {
            mHolder = (Holder) view.getTag();
        }
        if (mUids != null) {
            mHolder.mTvUid.setText(mUids.get(i) + "");
            mHolder.mBtnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onConfirmClicked(mUids.get(i));
                    }
                }
            });
            mHolder.mBtnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mListener != null) {
                        mListener.onCancelClicked(mUids.get(i));
                    }
                }
            });
        }
        return view;
    }
}

class Holder {
    TextView mTvUid;
    Button mBtnCancel;
    Button mBtnConfirm;
}