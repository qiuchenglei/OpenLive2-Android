package io.agora.message;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import io.agora.R;


public class InChannelMessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    protected final LayoutInflater mInflater;
    private final Context mContext;
    private ArrayList<Message> mMsglist;

    public InChannelMessageListAdapter(Context context, ArrayList<Message> list) {
        mContext = context;
        mInflater = ((Activity) context).getLayoutInflater();
        mMsglist = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.in_channel_message, parent, false);
        return new MessageHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message msg = mMsglist.get(position);

        MessageHolder myHolder = (MessageHolder) holder;

        myHolder.itemView.setBackgroundResource(R.drawable.rounded_bg);
        myHolder.msgContent.setText(msg.getContent());
    }

    @Override
    public int getItemCount() {
        return mMsglist.size();
    }

    @Override
    public long getItemId(int position) {
        return mMsglist.get(position).hashCode();
    }

    public class MessageHolder extends RecyclerView.ViewHolder {
        public TextView msgContent;

        public MessageHolder(View v) {
            super(v);
            msgContent = (TextView) v.findViewById(R.id.msg_content);
        }
    }
}
