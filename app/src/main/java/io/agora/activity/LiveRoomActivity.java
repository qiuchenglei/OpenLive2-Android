package io.agora.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import io.agora.R;
import io.agora.account.AccountAdapter;
import io.agora.account.AccountData;
import io.agora.account.IAccountClickListener;
import io.agora.cdn.CdnAdapter;
import io.agora.cdn.ICdnClickListener;
import io.agora.linker.LinkAdapter;
import io.agora.linker.LinkListener;
import io.agora.live.LiveChannelConfig;
import io.agora.live.LiveEngine;
import io.agora.live.LiveEngineHandler;
import io.agora.live.LivePublisher;
import io.agora.live.LivePublisherHandler;
import io.agora.live.LiveStats;
import io.agora.live.LiveSubscriber;
import io.agora.live.LiveSubscriberHandler;
import io.agora.message.InChannelMessageListAdapter;
import io.agora.message.Message;
import io.agora.message.MessageListDecoration;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.utils.CommonFunc;
import io.agora.utils.ConstantApp;
import io.agora.utils.LinkState;
import io.agora.video.GridVideoViewContainer;

import static io.agora.activity.LiveRoomActivity.AgoraDialog.DIALOG_TYPE_ACTION;
import static io.agora.activity.LiveRoomActivity.AgoraDialog.DIALOG_TYPE_WATI;

public class LiveRoomActivity extends AppCompatActivity implements View.OnClickListener,
        IAccountClickListener,
        ICdnClickListener {
    private static final String TAG = LiveRoomActivity.class.getSimpleName();
    private final HashMap<Integer, SurfaceView> mUidsList = new HashMap<>();
    private GridVideoViewContainer mVideoContainer;
    private ImageView mIvUserAccount;
    private ImageView mIvCloseVideo;
    private TextView mTvRoomName;
    private ListView mLvInfo;
    private ListView mLvCdnList;
    private PopupWindow mCdnPopWindow;
    private List<String> mCdnDataSet;
    private CdnAdapter mCdnAdpapter;
    private AlertDialog mCdnDialog;
    private Button mBtnPublishCdn;
    private EditText mEtCdnUrl;
    private ImageView mIvPublish;
    private ImageView mIvLinker;
    private ImageView mIvChangeCamera;
    private ImageView mIvMute;
    private ImageView mIvCnd;
    private ImageView mIvAddCdn;
    private PopupWindow mAccountPopWindow;
    private ListView mLvAccountList;
    private List<AccountData> mAccountDataSet;
    private AccountAdapter mAccountAdapter;
    //live engine init
    private LiveEngine mAgoraLiveEngine;
    //live subscriber
    private LiveSubscriber mAgoraLiveSubscriber;
    private LivePublisher mAgoraLivePublisher;
    private String mRoomName = "";
    //audio mute status false-mute true-unMute
    private boolean mMuteStatus = true;
    private android.support.v7.app.AlertDialog mLinkDialog;
    private android.support.v7.app.AlertDialog.Builder mLinkDialogBuilder;
    private ProgressDialog mPLinkDialog;
    private Vibrator vibrator;
    private LinkAdapter mLinkAdapter;
    private ArrayList<Integer> mAllowedUserList;
    private InChannelMessageListAdapter mMessageAdapter;
    private ArrayList<Message> mMessageList;
    /**
     * init Agora LivePublisherHandler
     */
    private LivePublisherHandler mAgoraLivePublisherHandler = new LivePublisherHandler() {
        @Override
        public void onStreamUrlPublished(String url) {
            super.onStreamUrlPublished(url);
            sendMessageOnMainThread(new Message("推流成功：" + url));
        }

        @Override
        public void onPublishStreamUrlFailed(String url, int errorCode) {
            super.onPublishStreamUrlFailed(url, errorCode);
            sendMessageOnMainThread(new Message("推流失败：" + url));
        }

        @Override
        public void onStreamUrlUnpublished(String url) {
            super.onStreamUrlUnpublished(url);
            sendMessageOnMainThread(new Message("停止推流：" + url));
        }

        @Override
        public void onPublisherTranscodingUpdated(LivePublisher publisher) {
            super.onPublisherTranscodingUpdated(publisher);
        }

        @Override
        public void publishingRequestReceived(final int uid) {
            super.publishingRequestReceived(uid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mLinkAdapter != null) {
                        mLinkAdapter.addLinkers(uid);

                    }
                }
            });
            sendMessageOnMainThread(new Message("收到连麦请求：" + uid));
        }
    };

    /**
     * init Agora LiveSubscriberHandler
     */
    private LiveSubscriberHandler mAgoraLiveSubcriberHandler = new LiveSubscriberHandler() {

        @Override
        public void publishedByHost(final int uid, final int streamType) {
            super.publishedByHost(uid, streamType);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAccountAdapter.addAccount(uid, false, streamType, LinkState.RequestLink);
                }
            });
            sendMessageOnMainThread(new Message(uid + "开播了"));
        }

        @Override
        public void unpublishedByHost(final int uid) {
            super.unpublishedByHost(uid);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAccountAdapter.removeAccount(uid);
                    mLinkAdapter.removeLinkers(uid);
                    if (mUidsList.containsKey(uid)) {
                        mUidsList.remove(uid);
                        if (mUidsList.size() == 0)
                            mVideoContainer.initViewContainer(LiveRoomActivity.this, 0, mUidsList);
                        else
                            mVideoContainer.initViewContainer(LiveRoomActivity.this, mUidsList.keySet().iterator().next(), mUidsList);
                    }
                }
            });
            sendMessageOnMainThread(new Message(uid + "闭播了"));
        }


        @Override
        public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
            super.onFirstRemoteVideoDecoded(uid, width, height, elapsed);
        }

        @Override
        public void onVideoSizeChanged(int uid, int width, int height, int rotation) {
            super.onVideoSizeChanged(uid, width, height, rotation);
        }

        @Override
        public void onStreamTypeChanged(int streamType, int uid) {
            super.onStreamTypeChanged(streamType, uid);
        }


    };

    /**
     * init LiveEngineHandler
     */
    private LiveEngineHandler mAgoraLiveEngineHandler = new LiveEngineHandler() {

        @Override
        public void onWarning(int warningCode) {
            //super.onWarning(warningCode);
            Log.e("onJoinChannel-->warning", warningCode + "");
        }

        @Override
        public void onError(int errorCode) {
            //super.onError(errorCode);
        }

        @Override
        public void onJoinChannel(String channel, int uid, int elapsed) {
            //super.onJoinChannel(channel, uid, elapsed);

            Log.e(TAG, "onJoinChannel-->" + channel + " , " + uid + " , " + elapsed);
            sendMessageOnMainThread(new Message("加入频道：" + channel));
        }

        @Override
        public void onLeaveChannel() {
            //super.onLeaveChannel();
            Log.e("onChannel-->", "leave success");
            sendMessageOnMainThread(new Message("离开频道"));
        }

        @Override
        public void onRejoinChannel(String channel, int uid, int elapsed) {
            //super.onRejoinChannel(channel, uid, elapsed);
            Log.e(TAG, "onRejoinChannel-->" + channel + " , " + uid + " , " + elapsed);
            sendMessageOnMainThread(new Message("重新加入频道：" + channel));
        }

        @Override
        public void onReportLiveStats(LiveStats stats) {
            super.onReportLiveStats(stats);
        }

        @Override
        public void onConnectionInterrupted() {
            //super.onConnectionInterrupted();
            Log.e(TAG, "onConnectionInterrupted-->");
            sendMessageOnMainThread(new Message("连接被打断"));
        }

        @Override
        public void onConnectionLost() {
            super.onConnectionLost();
            Log.e(TAG, "onConnectionLost-->");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dismissDialog();
                    if (mAccountDataSet != null) {
                        for (int i = 0; i < mAccountDataSet.size(); i++) {
                            mAccountDataSet.get(i).mLinkState = LinkState.RequestLink;
                        }

                        mAccountAdapter.notifyDataSetChanged();
                    }
                }
            });

            sendMessageOnMainThread(new Message("失去连接"));
        }

        @Override
        public void onNetworkQuality(int uid, int txQuality, int rxQuality) {
            super.onNetworkQuality(uid, txQuality, rxQuality);
        }

        @Override
        public void onRequestChannelKey() {
            super.onRequestChannelKey();
        }

        @Override
        public void publishingRequestAnswered(final int ownerUid, final boolean accepted, final int error) {
            super.publishingRequestAnswered(ownerUid, accepted, error);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int index = -1;
                    for (int i = 0; i < mAccountDataSet.size(); i++) {
                        if (mAccountDataSet.get(i).id == ownerUid)
                            index = i;
                    }

                    if (index != -1) {
                        if (accepted) {
                            mAccountDataSet.get(index).mLinkState = LinkState.ReqesuestT;
                            //new String((ownerUid & 0xFFFFFFFFL) + "接收连麦")));
                            sendMessageOnMainThread(new Message(ownerUid + "接受请求"));
                        } else {
                            mAccountDataSet.get(index).mLinkState = LinkState.RequestLink;
                            //new String((ownerUid & 0xFFFFFFFFL) + "拒绝连麦")));
                            sendMessageOnMainThread(new Message(ownerUid + "拒绝请求"));
                        }

                        if (error == 0) {
                        }
                    }
                }
            });
        }

        @Override
        public void unpublishingRequestReceived(int ownerUid) {
            super.unpublishingRequestReceived(ownerUid);
            //被T下麦
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mAccountDataSet != null) {
                        for (int i = 0; i < mAccountDataSet.size(); i++) {
                            mAccountDataSet.get(i).mLinkState = LinkState.RequestLink;
                        }

                        mAccountAdapter.notifyDataSetChanged();
                    }

                }
            });

            sendMessageOnMainThread(new Message("被" + ownerUid + "移除"));

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_room);

        mRoomName = getIntent().getStringExtra(ConstantApp.MAIN_TO_LIVE_ROOM);

        initWidget();
        initAccountPop();
        initCdnPop();
        initDataSet();
        initEngine();
        registerListener();
        joinChannel();
    }

    /**
     * init widget
     */
    public void initWidget() {
        mVideoContainer = (GridVideoViewContainer) findViewById(R.id.grid_video_view_container);
        mVideoContainer.initViewContainer(LiveRoomActivity.this, 0, mUidsList);
        mIvUserAccount = (ImageView) findViewById(R.id.iv_user_account);
        mIvCloseVideo = (ImageView) findViewById(R.id.iv_close_video);

        mIvUserAccount.setOnClickListener(this);

        mTvRoomName = (TextView) findViewById(R.id.tv_room_name_live);
        initMessageList();
        mIvLinker = (ImageView) findViewById(R.id.iv_live_room_linker);
        mIvLinker.setOnClickListener(this);

        mIvPublish = (ImageView) findViewById(R.id.iv_live_room_publish);
        mIvChangeCamera = (ImageView) findViewById(R.id.iv_live_room_change_camera);
        mIvMute = (ImageView) findViewById(R.id.iv_live_room_mute);

        mIvCnd = (ImageView) findViewById(R.id.iv_live_room_cdn);
        mTvRoomName.setText(mRoomName);

        mAllowedUserList = new ArrayList<>();
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mLinkAdapter = new LinkAdapter(new WeakReference<Context>(this), new ArrayList<Integer>(), new LinkListener() {
            @Override
            public void onConfirmClicked(final int uid) {
//                vibrator.vibrate(500);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAgoraLivePublisher.answerPublishingRequest(uid, true);
                        if (!mAllowedUserList.contains(uid))
                            mAllowedUserList.add(new Integer(uid));

                        mLinkAdapter.removeLinkers(uid);
                        //new String("确认" + (uid & 0xFFFFFFFFL) + "的连麦")));

                    }
                });

                sendMessageOnMainThread(new Message("确认" + uid + "请求"));
            }

            @Override
            public void onCancelClicked(final int uid) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAgoraLivePublisher.answerPublishingRequest(uid, false);
                        mLinkAdapter.removeLinkers(uid);
                        // new String("接收" + (uid & 0xFFFFFFFFL) + "的连麦")));
                    }
                });

                sendMessageOnMainThread(new Message("拒绝" + uid + "请求"));
            }

            @Override
            public void onListEmptyNotify() {
                if (mLinkDialog != null && mLinkDialog.isShowing())
                    mLinkDialog.cancel();
            }

        });

    }

    /**
     * init user account window
     */
    public void initAccountPop() {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup mAccountView = (ViewGroup) layoutInflater.inflate(R.layout.account_list, null);
        mLvAccountList = mAccountView.findViewById(R.id.lv_account_listview);
        mAccountPopWindow = new PopupWindow(mAccountView, CommonFunc.dip2px(this, 256), ViewGroup.LayoutParams.WRAP_CONTENT);
        mAccountPopWindow.setBackgroundDrawable(new BitmapDrawable());
        mAccountPopWindow.setOutsideTouchable(true);
        mAccountPopWindow.setOutsideTouchable(true);
        mAccountPopWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        mAccountPopWindow.setTouchable(true);
        mAccountPopWindow.setFocusable(true);
    }

    /**
     * init cdn window
     */
    public void initCdnPop() {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        ViewGroup mCndView = (ViewGroup) layoutInflater.inflate(R.layout.cdn_url_list, null);
        mLvCdnList = mCndView.findViewById(R.id.lv_cdn_list);
        mIvAddCdn = mCndView.findViewById(R.id.iv_cdn_bottom);
        mCdnPopWindow = new PopupWindow(mCndView, CommonFunc.dip2px(this, 164), CommonFunc.dip2px(this, 256));
        mCdnPopWindow.setBackgroundDrawable(new BitmapDrawable());
        mCdnPopWindow.setOutsideTouchable(true);
        mCdnPopWindow.setAnimationStyle(android.R.style.Animation_Dialog);
        mCdnPopWindow.setTouchable(true);
        mCdnPopWindow.setFocusable(true);
        initCndDialog();
    }

    public void initCndDialog() {
        mCdnDialog = new AlertDialog.Builder(this).create();
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.add_url_dialog, null);
        mCdnDialog.setView(dialogView);
        mBtnPublishCdn = dialogView.findViewById(R.id.btn_cdn_publish);
        mEtCdnUrl = dialogView.findViewById(R.id.et_cdn_url);
    }

    public void showDialog() {
        if (mCdnDialog != null && !mCdnDialog.isShowing())
            mCdnDialog.show();
    }

    /**
     * init data set
     */
    public void initDataSet() {
        mAccountDataSet = new ArrayList<>();
        mAccountAdapter = new AccountAdapter(this, mAccountDataSet, this);
        mLvAccountList.setAdapter(mAccountAdapter);

        mCdnDataSet = new ArrayList<>();
        mCdnAdpapter = new CdnAdapter(this, mCdnDataSet, this);
        mLvCdnList.setAdapter(mCdnAdpapter);
    }

    /**
     * init Agora engine
     */
    public void initEngine() {
        mAgoraLiveEngine = LiveEngine.createLiveEngine(getApplicationContext(), getResources().getString(R.string.private_app_id)
                , mAgoraLiveEngineHandler);

        mAgoraLivePublisher = new LivePublisher(mAgoraLiveEngine, mAgoraLivePublisherHandler);

        mAgoraLiveSubscriber = new LiveSubscriber(mAgoraLiveEngine, mAgoraLiveSubcriberHandler);


        mAgoraLiveEngine.getRtcEngine().setParameters("{\"rtc.log_filter\":65535}");
        mAgoraLiveEngine.getRtcEngine().setLogFile("/sdcard/open_live.log");
    }

    /**
     * register click listener
     */
    public void registerListener() {
        mIvPublish.setOnClickListener(this);
        mIvChangeCamera.setOnClickListener(this);
        mIvMute.setOnClickListener(this);
        mIvCloseVideo.setOnClickListener(this);
        mIvCnd.setOnClickListener(this);
        mIvAddCdn.setOnClickListener(this);
        mBtnPublishCdn.setOnClickListener(this);
    }

    private void joinChannel() {
        LiveChannelConfig mLiveChannelConfig = new LiveChannelConfig();
        mLiveChannelConfig.videoEnabled = true;
        int joinRet = mAgoraLiveEngine.joinChannel(mRoomName, getResources().getString(R.string.private_app_id), mLiveChannelConfig, 0);
        Log.e("onJoinRet-->", joinRet + "");
    }


    /**
     * when user account list had clicked , we could get Uid and subscript status here.
     *
     * @param id            uid
     * @param currentStatus 当前订阅状态
     * @param mediaType     流类型
     */
    @Override
    public void onAccountClick(final int id, final boolean currentStatus, int mediaType) {
        Log.e("onAccountClick-->", "id--" + id + " , isChecked-->" + currentStatus);

        if (isFinishing())
            return;

        if (currentStatus) {
            if (mUidsList.containsKey(id))
                return;

            SurfaceView surfaceV = RtcEngine.CreateRendererView(LiveRoomActivity.this);
            mUidsList.put(id, surfaceV);
            mVideoContainer.initViewContainer(LiveRoomActivity.this, id, mUidsList);
            mAgoraLiveSubscriber.subscribe(id, mediaType, surfaceV, Constants.RENDER_MODE_HIDDEN, Constants.VIDEO_STREAM_HIGH);
        } else {
            mAgoraLiveSubscriber.unsubscribe(id);
            mUidsList.remove(id);
            if (mUidsList.size() == 0)
                mVideoContainer.initViewContainer(LiveRoomActivity.this, 0, mUidsList);
            else
                mVideoContainer.initViewContainer(LiveRoomActivity.this, mUidsList.keySet().iterator().next(), mUidsList);
        }
    }

    @Override
    public void onLinkOrTClicked(final int uid, final LinkState currentState) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAccountAdapter != null) {
                    boolean isChange = false;
                    boolean currentIsChecked = false;
                    int mediaType = 0;
                    if (currentState == LinkState.RequestLink) {
                        mAgoraLiveEngine.sendPublishingRequest(uid);
                        if (mAccountAdapter.checkIsExisted(uid)) {
                            AccountData ad = mAccountAdapter.getAccountData(uid);
                            currentIsChecked = ad.isChecked;
                            mediaType = ad.mediaType;
                            mAccountAdapter.removeAccount(uid);
                        }

                        mAccountAdapter.addAccount(uid, currentIsChecked, mediaType, LinkState.Linking);
                        // new String("申请向" + (uid & 0xFFFFFFFFL) + "连麦")));
                        sendMessageOnMainThread(new Message("向" + uid + "申请连麦"));
                    } else if (currentState == LinkState.ReqesuestT) {
                        mAgoraLivePublisher.sendUnpublishingRequest(uid);
                        if (mAccountAdapter.checkIsExisted(uid)) {
                            AccountData ad = mAccountAdapter.getAccountData(uid);
                            currentIsChecked = ad.isChecked;
                            mediaType = ad.mediaType;
                            mAccountAdapter.removeAccount(uid);
                        }

                        mAccountAdapter.addAccount(uid, currentIsChecked, mediaType, LinkState.RequestLink);
                        //String("T除 " + (uid & 0xFFFFFFFFL))));
                        sendMessageOnMainThread(new Message("T除" + uid));
                    }
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_user_account:
                actionAccount();
                break;
            case R.id.iv_live_room_publish:
                if (mIvMute.getVisibility() == View.VISIBLE) {
                    enablePublisher(false);
                } else {
                    enablePublisher(true);
                }
                break;
            case R.id.iv_live_room_change_camera:
                mAgoraLivePublisher.switchCamera();
                break;
            case R.id.iv_live_room_mute:
                muteOrUnmuteAudio(!mMuteStatus);
                break;
            case R.id.iv_close_video:
                finish();
                break;
            case R.id.iv_live_room_cdn:
                actionCnd();
                break;
            case R.id.iv_cdn_bottom:
                showDialog();
                mCdnPopWindow.dismiss();
                break;
            case R.id.btn_cdn_publish:
                doPublishClick();
                break;
            case R.id.iv_live_room_linker:
                showMDialog(DIALOG_TYPE_ACTION);
                break;
            default:
                break;
        }
    }

    /**
     * when you click user account icon, show popwindow
     */
    public void actionAccount() {
        mAccountPopWindow.showAsDropDown(mIvUserAccount);
    }

    /**
     * when you click cdn icon, show popwindow
     */
    public void actionCnd() {
        mCdnPopWindow.showAtLocation(mIvCnd, Gravity.RIGHT | Gravity.BOTTOM, 0, CommonFunc.dip2px(this, 56));
    }

    /**
     * 开始或者停止推流
     *
     * @param enable true-start false-finish
     */
    public void enablePublisher(boolean enable) {
        showOrHideBottomViewBar(enable);
        if (enable) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
            int prefIndex = pref.getInt(ConstantApp.PrefManager.PREF_PROPERTY_PROFILE_IDX, ConstantApp.DEFAULT_PROFILE_IDX);
            if (prefIndex > ConstantApp.VIDEO_PROFILES.length - 1) {
                prefIndex = ConstantApp.DEFAULT_PROFILE_IDX;
            }
            int vProfile = ConstantApp.VIDEO_PROFILES[prefIndex];
            int frameRate = ConstantApp.VIDEO_PROFILES_FRAMERATE[prefIndex];
            int bitRate = ConstantApp.VIDEO_PROFILES_BITRATE[prefIndex];

            int width = mVideoContainer.getWidth();
            int height = mVideoContainer.getHeight();

            int tempWidth = width;
            int tempHeigh = height;
            if (mUidsList.size() == 1) {
                tempWidth = width;
                tempHeigh = height / 2;
            } else if (mUidsList.size() == 2 || mUidsList.size() == 3) {
                tempWidth = width / 2;
                tempHeigh = height / 2;
            } else if (mUidsList.size() > 3) {
                return;
            }

            mAgoraLivePublisher.setVideoProfile(tempWidth, tempHeigh, frameRate, bitRate);
            mAgoraLivePublisher.setMediaType(Constants.MEDIA_TYPE_AUDIO_AND_VIDEO);
            mAgoraLivePublisher.publishWithPermissionKey("");

            SurfaceView mAgoraSurfaceView = RtcEngine.CreateRendererView(LiveRoomActivity.this);
            mAgoraSurfaceView.setZOrderOnTop(true);
            mAgoraSurfaceView.setZOrderMediaOverlay(true);

            mUidsList.put(0, mAgoraSurfaceView);
            mVideoContainer.initViewContainer(LiveRoomActivity.this, 0, mUidsList);
            mAgoraLiveEngine.startPreview(mAgoraSurfaceView, Constants.RENDER_MODE_HIDDEN);
        } else {
            LiveChannelConfig mLiveChannelConfig = new LiveChannelConfig();
            mLiveChannelConfig.videoEnabled = true;
            mAgoraLiveEngine.stopPreview();
            mAgoraLivePublisher.unpublish();
            mUidsList.remove(0);
            if (mUidsList.size() == 0)
                mVideoContainer.initViewContainer(this, 0, mUidsList);
            else
                mVideoContainer.initViewContainer(this, mUidsList.keySet().iterator().next(), mUidsList);
        }
    }

    /**
     * 隐藏或者显示底部按钮
     *
     * @param show ture-show false-hide
     */
    public void showOrHideBottomViewBar(boolean show) {
        if (show) {
            mIvPublish.setImageResource(R.drawable.btn_join_cancel);
            mIvMute.setVisibility(View.VISIBLE);
            mIvChangeCamera.setVisibility(View.VISIBLE);
            mIvCnd.setVisibility(View.VISIBLE);
        } else {
            mIvPublish.setImageResource(R.drawable.btn_join_request);
            mIvMute.setVisibility(View.GONE);
            mIvChangeCamera.setVisibility(View.GONE);
            mIvCnd.setVisibility(View.GONE);
        }
    }

    /**
     * mute or unmute local audio
     *
     * @param mute false-unmute true-mute
     */
    public void muteOrUnmuteAudio(boolean mute) {
        if (mute) {
            mIvMute.setImageResource(R.drawable.btn_mute);
            mAgoraLivePublisher.setMediaType(Constants.MEDIA_TYPE_AUDIO_AND_VIDEO);
        } else {
            mIvMute.setImageResource(R.drawable.btn_mute_cancel);
            mAgoraLivePublisher.setMediaType(Constants.MEDIA_TYPE_VIDEO_ONLY);
        }

        mMuteStatus = mute;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAgoraLiveEngine != null) {

                    if (mUidsList.containsKey(0)) {
                        mAgoraLiveEngine.stopPreview();
                        mAgoraLivePublisher.unpublish();
                        mUidsList.remove(0);
                    }

                    if (mUidsList.size() != 0) {
                        Iterator<Integer> it = mUidsList.keySet().iterator();
                        while (it.hasNext()) {
                            mAgoraLiveSubscriber.unsubscribe(it.next());
                        }
                    }

                    mUidsList.clear();
                    mAgoraLiveEngine.leaveChannel();
                    mMessageList.clear();
                }
            }
        });

    }

    @Override
    public void OnCdnItemClickListener(String url, boolean isPush) {
        if (mAgoraLivePublisher != null) {
            if (isPush) {
                mAgoraLivePublisher.addStreamUrl(url, false);
            } else {
                mAgoraLivePublisher.removeStreamUrl(url);
                mAgoraLivePublisher.removeStreamUrl(url);
            }
        }
    }

    public void doPublishClick() {
        if (mCdnDialog.isShowing())
            mCdnDialog.dismiss();

        RtcEngine rtcEngine = mAgoraLiveEngine.getRtcEngine();
        rtcEngine.setExternalVideoSource(true, true, true);
        if (TextUtils.isEmpty(mEtCdnUrl.getText()))
            return;

        String url = mEtCdnUrl.getText().toString();
        if (!mCdnDataSet.contains(url)) {
            mCdnDataSet.add(url);
            mCdnAdpapter.notifyDataSetChanged();
        }

        int positon = -1;
        for (int i = 0; i < mCdnDataSet.size(); i++) {
            if (mCdnDataSet.get(i).equals(url)) {
                positon = i;
                break;
            }
        }
        mCdnAdpapter.setPostion(positon);

        if (mAgoraLivePublisher != null) {
            mAgoraLivePublisher.addStreamUrl(url, false);
        }
    }

    public void showMDialog(AgoraDialog type) {
        if (mLinkDialog != null && mLinkDialog.isShowing()) {
            mLinkDialog.dismiss();
            mLinkDialog = null;
        }

        if (mPLinkDialog != null && mPLinkDialog.isShowing()) {
            mPLinkDialog.dismiss();
            mPLinkDialog = null;
        }

        if (type == DIALOG_TYPE_ACTION) {

            LayoutInflater inflater = LayoutInflater.from(this);
            View layout = inflater.inflate(R.layout.dialog_link, null, false);
            ListView linkLv = (ListView) layout.findViewById(R.id.lv_dialog_link);
            linkLv.setAdapter(mLinkAdapter);

            if (mLinkDialogBuilder == null) {
                mLinkDialogBuilder = new android.support.v7.app.AlertDialog.Builder(this);
                mLinkDialogBuilder.setCancelable(true);
                mLinkDialogBuilder.setView(layout);

                mLinkDialog = mLinkDialogBuilder.create();
            }
            mLinkDialog.show();
        } else if (type == DIALOG_TYPE_WATI) {
            mPLinkDialog = new ProgressDialog(this);
            mPLinkDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mPLinkDialog.setCancelable(false);
            mPLinkDialog.setCanceledOnTouchOutside(false);
            mPLinkDialog.setMessage("连麦申请：");
            mPLinkDialog.show();
        }
    }

    public void dismissDialog() {
        if (mLinkDialog != null) {
            mLinkDialog.dismiss();
            mLinkDialog = null;
        }

        if (mPLinkDialog != null) {
            mPLinkDialog.dismiss();
            mPLinkDialog = null;
        }
    }

    private void initMessageList() {
        mMessageList = new ArrayList<>();
        RecyclerView msgListView = (RecyclerView) findViewById(R.id.msg_list);

        mMessageAdapter = new InChannelMessageListAdapter(this, mMessageList);
        mMessageAdapter.setHasStableIds(true);

        msgListView.setAdapter(mMessageAdapter);
        msgListView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        msgListView.addItemDecoration(new MessageListDecoration());
    }

    private void sendMessageOnMainThread(final Message message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendMessage(message);
            }
        });
    }

    private void sendMessage(Message msg) {
        mMessageList.add(msg);

        int MAX_MESSAGE_COUNT = 16;

        if (mMessageList.size() > MAX_MESSAGE_COUNT) {
            int toRemove = mMessageList.size() - MAX_MESSAGE_COUNT;
            for (int i = 0; i < toRemove; i++) {
                mMessageList.remove(i);
            }
        }

        mMessageAdapter.notifyDataSetChanged();
    }


    public enum AgoraDialog {
        DIALOG_TYPE_WATI,
        DIALOG_TYPE_ACTION
    }

}
