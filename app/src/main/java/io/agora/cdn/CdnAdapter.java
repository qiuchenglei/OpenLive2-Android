package io.agora.cdn;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import io.agora.R;

public class CdnAdapter extends BaseAdapter {
    private Context mContext;
    private List<String> mData;
    private LayoutInflater mInflater;
    private ICdnClickListener mCdnClickListener;
    private int mClickPostion = -1;

    public CdnAdapter(Context context, List<String> data, ICdnClickListener listener) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mCdnClickListener = listener;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {

        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        MyHolder holder;
        if (view == null) {
            view = mInflater.inflate(R.layout.cdn_url_list_item, null);
            holder = new MyHolder();
            holder.mtv = view.findViewById(R.id.tv_cdn_url);

            view.setTag(holder);
        } else {
            holder = (MyHolder) view.getTag();
        }

        holder.mtv.setText(mData.get(i));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (i == mClickPostion)
                    mClickPostion = -1;
                else
                    mClickPostion = i;

                if (mCdnClickListener != null)
                    mCdnClickListener.OnCdnItemClickListener(mData.get(i), !(mClickPostion == -1));
                notifyDataSetChanged();
            }
        });

        if (mClickPostion == i) {
            view.setBackgroundColor(mContext.getResources().getColor(R.color.colorAccent));
        } else {
            view.setBackgroundColor(mContext.getResources().getColor(R.color.co));
        }

        return view;
    }

    public void setPostion(int p) {
        mClickPostion = p;
    }

    class MyHolder {
        public TextView mtv;
    }
}
