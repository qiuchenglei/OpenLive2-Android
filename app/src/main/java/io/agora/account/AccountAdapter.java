package io.agora.account;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import io.agora.R;
import io.agora.utils.LinkState;

public class AccountAdapter extends BaseAdapter {
    private Context mContext;
    private List<AccountData> mData;
    private LayoutInflater mInflater;

    private IAccountClickListener mAccountListener;

    public AccountAdapter(Context context, List<AccountData> mData, IAccountClickListener listener) {
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = mData;
        this.mAccountListener = listener;
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
        MyHolder holder = null;
        if (view == null) {
            view = mInflater.inflate(R.layout.account_list_item, null);
            holder = new MyHolder();
            holder.mtv = view.findViewById(R.id.tv_account_item_id);
            holder.mcb = view.findViewById(R.id.cb_account_item_checked);

            holder.mLinkState = view.findViewById(R.id.tv_user_account_link_state);
            holder.mLinkStatePb = view.findViewById(R.id.pb_link_state);

            view.setTag(holder);
        } else {
            holder = (MyHolder) view.getTag();
        }
        holder.mcb.setChecked(mData.get(i).isChecked);
        holder.mtv.setText(mData.get(i).id + "");

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAccountListener != null) {
                    mAccountListener.onAccountClick(mData.get(i).id, !mData.get(i).isChecked, mData.get(i).mediaType);
                    addAccount(mData.get(i).id, !mData.get(i).isChecked, mData.get(i).mediaType, mData.get(i).mLinkState);
                }
                notifyDataSetChanged();
            }
        });

        if (mData.get(i).mLinkState == LinkState.RequestLink) {
            holder.mLinkStatePb.setVisibility(View.GONE);
            holder.mLinkState.setText(mContext.getResources().getString(R.string.link_requests));
            holder.mLinkState.setVisibility(View.VISIBLE);
            holder.mLinkState.setEnabled(true);
        } else if (mData.get(i).mLinkState == LinkState.Linking) {
            holder.mLinkState.setVisibility(View.GONE);
            holder.mLinkStatePb.setVisibility(View.VISIBLE);
        } else if (mData.get(i).mLinkState == LinkState.ReqesuestT) {
            holder.mLinkStatePb.setVisibility(View.GONE);
            holder.mLinkState.setText(mContext.getResources().getString(R.string.link_force_t));
            holder.mLinkState.setVisibility(View.VISIBLE);
            holder.mLinkState.setEnabled(true);
        }

        holder.mLinkState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(subscribeCallback != null){
//                    if(mData.get(position).mLinkState == LinkState.RequestLink){
//                        mData.get(position).mLinkState = LinkState.Linking ;
//                        subscribeCallback.onLinkOrTClicked(Integer.valueOf(mData.get(position).url).intValue(), LinkState.Linking);
//                    }
//                }
                mAccountListener.onLinkOrTClicked(mData.get(i).id, mData.get(i).mLinkState);
            }
        });
        return view;
    }

    public void addAccount(@NonNull int id, boolean isChecked, int mediaT, LinkState linkState) {
        boolean existed = false;
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).id == id) {
                mData.get(i).isChecked = isChecked;

                existed = true;
                break;
            }
        }

        if (!existed) {
            AccountData ad = new AccountData(id, isChecked, mediaT, linkState);
            mData.add(ad);
        }

        notifyDataSetChanged();
    }

    public void removeAccount(int id) {
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).id == id) {
                mData.remove(i);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public boolean checkIsExisted(int id) {
        boolean existed = false;
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).id == id) {
                existed = true;
                break;
            }
        }

        return existed;
    }

    public AccountData getAccountData(int uid) {
        int index = -1;
        for (int i = 0; i < mData.size(); i++) {
            if (mData.get(i).id == uid) {
                index = i;
                break;
            }
        }
        if (index == -1)
            return null;

        return mData.get(index);
    }

    public List<AccountData> getAllAccount() {
        return mData;
    }

    class MyHolder {
        TextView mtv;
        CheckBox mcb;
        TextView mLinkState;
        ProgressBar mLinkStatePb;
    }
}
