package io.agora.video;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.agora.R;

public class VideoProfileAdapter extends RecyclerView.Adapter {
    private Context mContext;
    private int seleted;

    public VideoProfileAdapter(Context mContext, int seleted) {
        this.mContext = mContext;
        this.seleted = seleted;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_profile_item, parent, false);
        ProfilesHolder profilesHolder = new ProfilesHolder(v);
        return profilesHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String resolution = mContext.getResources().getStringArray(R.array.string_array_resolutions)[position];
        ((ProfilesHolder) holder).resolution.setText(resolution);

        String frameRate = mContext.getResources().getStringArray(R.array.string_array_frame_rate)[position];
        ((ProfilesHolder) holder).frameRate.setText(frameRate);

        String bitRate = mContext.getResources().getStringArray(R.array.string_array_bit_rate)[position];
        ((ProfilesHolder) holder).bitRate.setText(bitRate);

        holder.itemView.setBackgroundResource(position == seleted ? R.color.lightColorAccent : android.R.color.transparent);
    }

    @Override
    public int getItemCount() {
        return mContext.getResources().getStringArray(R.array.string_array_resolutions).length;
    }

    public int getSeleted() {
        return seleted;
    }

    public class ProfilesHolder extends RecyclerView.ViewHolder {
        public TextView resolution;
        public TextView frameRate;
        public TextView bitRate;

        public ProfilesHolder(View itemView) {
            super(itemView);

            resolution = itemView.findViewById(R.id.resolution);
            frameRate = itemView.findViewById(R.id.frame_rate);
            bitRate = itemView.findViewById(R.id.bit_rate);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    seleted = getLayoutPosition();
                    notifyDataSetChanged();
                }
            });
        }
    }
}
