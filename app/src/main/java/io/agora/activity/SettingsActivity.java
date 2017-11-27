package io.agora.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import io.agora.R;
import io.agora.utils.ConstantApp;
import io.agora.video.VideoProfileAdapter;


public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {
    private VideoProfileAdapter mvpa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initWidget();
    }

    private void initWidget() {
        RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.profiles);
        mRecyclerView.setHasFixedSize(true);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        int prefIndex = pref.getInt(ConstantApp.PrefManager.PREF_PROPERTY_PROFILE_IDX, ConstantApp.DEFAULT_PROFILE_IDX);

        mvpa = new VideoProfileAdapter(this, prefIndex);
        mvpa.setHasStableIds(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setAdapter(mvpa);

        findViewById(R.id.btn_confirm_setting).setOnClickListener(this);
    }


    private void doSaveProfile() {
        int profileIndex = mvpa.getSeleted();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(ConstantApp.PrefManager.PREF_PROPERTY_PROFILE_IDX, profileIndex);
        editor.apply();

        Log.e("doSaveProfile-->", "confirm setting");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm_setting:
                doSaveProfile();
                onBackPressed();
                break;
        }
    }
}
