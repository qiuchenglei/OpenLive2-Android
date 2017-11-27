package io.agora.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import io.agora.R;
import io.agora.utils.CommonFunc;
import io.agora.utils.ConstantApp;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener {
    //widget
    private EditText mEtRoomName;
    private Button mBtnJoinRoom;
    private ImageView mIvSetting;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (shouldAskPermissions()) {
            askPermissions();
        }

        setContentView(R.layout.activity_main);
        initHideSoftKeyBoard();
        initWidget();
    }

    private void initHideSoftKeyBoard() {
        RelativeLayout v = (RelativeLayout) findViewById(R.id.rl_m);
        v.setOnTouchListener(this);
    }

    private void initWidget() {
        /**
         * Room_name
         */
        mEtRoomName = (EditText) findViewById(R.id.tv_room_name);

        /**
         * setting button
         */
        mIvSetting = (ImageView) findViewById(R.id.btn_setting);
        mIvSetting.setOnClickListener(this);


        /**
         * join button
         */
        mBtnJoinRoom = (Button) findViewById(R.id.btn_join);
        mBtnJoinRoom.setOnClickListener(this);
    }

    public void forwardToSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void forwardToLivRoomActivity() {
        if (mEtRoomName.getText().toString().isEmpty()
                && mEtRoomName.getText().toString().trim().equals("")) {
            Toast.makeText(MainActivity.this, "Please input room name of the Live.", Toast.LENGTH_SHORT).show();
            return;
        }

        String roomName = mEtRoomName.getText().toString();
        Intent i = new Intent(MainActivity.this, LiveRoomActivity.class);
        i.putExtra(ConstantApp.MAIN_TO_LIVE_ROOM, roomName);
        startActivity(i);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        CommonFunc.hideSoftKeyboard(this);
        return false;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_setting:
                forwardToSettingsActivity();
                break;
            case R.id.btn_join:
                forwardToLivRoomActivity();
                break;
            default:
                break;
        }
    }

    protected boolean shouldAskPermissions() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @TargetApi(23)
    protected void askPermissions() {
        String[] permissions = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.INTERNET",
                "android.permission.RECORD_AUDIO",
                "android.permission.CAMERA",
                "android.permission.MODIFY_AUDIO_SETTINGS",
                "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.BLUETOOTH"
        };
        int requestCode = 200;
        requestPermissions(permissions, requestCode);
    }


}

