package tk.wenop.XiangYu.ui.wenui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.im.BmobRecordManager;
import cn.bmob.im.inteface.OnRecordChangeListener;
import cn.bmob.im.util.BmobLog;
import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.SaveListener;
import tk.wenop.XiangYu.R;
import tk.wenop.XiangYu.adapter.NewRecordPlayClickListener;
import tk.wenop.XiangYu.adapter.custom.CommentAdapter;
import tk.wenop.XiangYu.adapter.custom.MainScreenOverviewItem;
import tk.wenop.XiangYu.bean.CommentEntity;
import tk.wenop.XiangYu.bean.MessageEntity;
import tk.wenop.XiangYu.bean.User;
import tk.wenop.XiangYu.manager.DBManager;
import tk.wenop.XiangYu.network.CommentNetwork;
import tk.wenop.XiangYu.util.CommonUtils;
import tk.wenop.XiangYu.util.WrappingRecyclerViewLayoutManager;


public class CommentActivity extends AppCompatActivity implements CommentNetwork.OnGetCommentEntities {

    private CommentAdapter commentAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mRVLayoutM;

    protected ArrayList<MainScreenOverviewItem> commentDataSet;


    public static MessageEntity messageEntity = null;
    ActionBar actionBar;

    // wenop-update 不能用inject了，因为我动态改变了View
//    @ViewInject(R.id.imageView_contentPhoto)
    ImageView iv_contentPhoto;
    ImageView iv_avatar;
    TextView tv_nickName;
    View card_root_view;
    TextView mCommentCount;
    View audio_bubble;

    ImageLoader imageLoader;
    User currentUser;
    String userID;
    Context context;

    // 语音有关
    @ViewInject(R.id.btn_speak)
    Button btn_speak;
    @ViewInject(R.id.layout_record)
    RelativeLayout layout_record;
    @ViewInject(R.id.tv_voice_tips)
    TextView tv_voice_tips;
    @ViewInject(R.id.iv_record)
    ImageView iv_record;
    @ViewInject(R.id.comment_content)
    ViewStub comment_content;

    String audioPath;

    private Drawable[] drawable_Anims;// 话筒动画
    BmobRecordManager recordManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (messageEntity == null) return; // TODO 完善?

        setContentView(R.layout.activity_comment);
        ViewUtils.inject(this);

        // 显示出返回按钮
        actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        currentUser = BmobUser.getCurrentUser(this, User.class);
        userID = currentUser.getObjectId();
        context = this;

        imageLoader = ImageLoader.getInstance();

        inflateCommentLayout();
        initView();

        /// 设置评论数据
        //设置recyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.comment_recyclerview);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
//        mRecyclerView.setHasFixedSize(true);

        // 用这个WrappingRecyclerViewLayoutManager改版的LayoutManager是因为我需要让
        //    这个recyclerView的内容展开(全局滚动)
        mRVLayoutM = new WrappingRecyclerViewLayoutManager(this);
        mRecyclerView.setLayoutManager(mRVLayoutM);

        commentDataSet = new ArrayList<>();
        commentAdapter = new CommentAdapter(CommentActivity.this);
        mRecyclerView.setAdapter(commentAdapter);

        //加载评论信息
        DBManager.instance(this).getComments(messageEntity, this);


    }

    private void inflateCommentLayout() {

        // wenop-mod
        // 根据messageEntity msgType来选择layout_include
        switch (messageEntity.getMsgType()) {
            case MessageEntity.MSG_TYPE_ONLY_PHOTO:
                comment_content.setLayoutResource(R.layout.content_comment_photo);
                break;
            case MessageEntity.MSG_TYPE_ONLY_AUDIO:
                comment_content.setLayoutResource(R.layout.content_comment_audio);
                audioPath = "http://file.bmob.cn/" + messageEntity.getAudio();
                break;
            case MessageEntity.MSG_TYPE_AUDIO_wITH_PHOTO:
                comment_content.setLayoutResource(R.layout.content_comment_both);
                break;
            default:

        }
        comment_content.inflate();
    }

    private void initView(){

        // inflate完 才绑定这些控件
        iv_avatar = (ImageView) findViewById(R.id.imageView_avatar);
        tv_nickName = (TextView) findViewById(R.id.tv_nickName);
        card_root_view = findViewById(R.id.card_root_view);
        mCommentCount = (TextView) findViewById(R.id.tv_comment_count);

        mCommentCount.setText(messageEntity.getCommentCount().toString());

        // 分消息类型来设置view
        iv_contentPhoto = (ImageView) findViewById(R.id.imageView_contentPhoto);
        audio_bubble = findViewById(R.id.audio_msg_bubble);
        ImageView iv_audioPlayAni = (ImageView) findViewById(R.id.iv_audio_play_ani);

        switch (messageEntity.getMsgType()) {
            case MessageEntity.MSG_TYPE_ONLY_PHOTO:
                imageLoader.displayImage("http://file.bmob.cn/" + messageEntity.getImage(), iv_contentPhoto);
                break;
            case MessageEntity.MSG_TYPE_ONLY_AUDIO:
                audioPath = "http://file.bmob.cn/" + messageEntity.getAudio();
                audio_bubble.setOnClickListener(
                    new NewRecordPlayClickListener(context, audioPath, iv_audioPlayAni));
                break;
            case MessageEntity.MSG_TYPE_AUDIO_wITH_PHOTO:
                imageLoader.displayImage("http://file.bmob.cn/" + messageEntity.getImage(), iv_contentPhoto);
                audioPath = "http://file.bmob.cn/" + messageEntity.getAudio();
                audio_bubble.setOnClickListener(
                        new NewRecordPlayClickListener(context, audioPath, iv_audioPlayAni));
                break;
            default:
        }

        if (messageEntity.getOwnerUser() != null){

            final User user = messageEntity.getOwnerUser();

            if (messageEntity.getAnonymous() == true) {
                // 匿名消息

                tv_nickName.setText("匿名用户");
                // 昵称样式
                tv_nickName.setTypeface(null, Typeface.ITALIC);

                // 消息的owner, 要根据性别设置样式
                if (messageEntity.getOwnerUser().getSex() == true)
                {
                    // 男
                    iv_avatar.setImageResource(R.drawable.avatar_a_m);
                    tv_nickName
                            .setTextColor(ContextCompat.getColor(context,
                                    R.color.anonymous_card_color_male));
//                    card_root_view
//                            .setBackgroundColor(ContextCompat.getColor(context,
//                                    R.color.anonymous_card_color_male));

                } else {
                    // 女
                    iv_avatar.setImageResource(R.drawable.avatar_a_fm);
                    tv_nickName
                            .setTextColor(ContextCompat.getColor(context,
                                    R.color.anonymous_card_color_female));
//                    card_root_view
//                            .setBackgroundColor(ContextCompat.getColor(context,
//                                    R.color.anonymous_card_color_female));
                }

                // 在actionBar显示
                actionBar.setTitle("匿名用户的消息");

            }
            else {
                // 非匿名消息

                if (user.getAvatar()!=null){
                    //渲染用户头像
                    imageLoader.displayImage(user.getAvatar(), iv_avatar);
                    iv_avatar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                         Intent intent = new Intent(context, PeopleDetailActivity.class);
//                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra("user", user);
                            context.startActivity(intent);
                        }
                    });
                }
                tv_nickName.setText(user.getNick());

                // 在actionBar显示
                actionBar.setTitle(user.getNick() + "说");

            }
        }

        initVoiceView();

    }
    private void initVoiceView() {

        layout_record = (RelativeLayout) findViewById(R.id.layout_record);
        tv_voice_tips = (TextView) findViewById(R.id.tv_voice_tips);
        iv_record = (ImageView) findViewById(R.id.iv_record);
        btn_speak.setOnTouchListener(new VoiceTouchListen());

        initVoiceAnimRes();
        initRecordManager();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // 如果是返回按钮，那就finish这个activity
        if (id==android.R.id.home) {
            finish();
        }

        return true;
    }


    @Override
    public void onGetCommentEntities(List<CommentEntity> allCommentEntities) {
        commentAdapter.putDataSet(allCommentEntities);
    }



    /*
        录音功能
     */


    private void initRecordManager(){
        // 语音相关管理器
        recordManager = BmobRecordManager.getInstance(this);
        // 设置音量大小监听--在这里开发者可以自己实现：当剩余10秒情况下的给用户的提示，类似微信的语音那样
        recordManager.setOnRecordChangeListener(new OnRecordChangeListener() {

            @Override
            public void onVolumnChanged(int value) {
                // TODO Auto-generated method stub
                iv_record.setImageDrawable(drawable_Anims[value]);
            }

            @Override
            public void onTimeChanged(int recordTime, String localPath) {
                // TODO Auto-generated method stub
                BmobLog.i("voice", "已录音长度:" + recordTime);
                if (recordTime >= BmobRecordManager.MAX_RECORD_TIME) {// 1分钟结束，发送消息
                    // 需要重置按钮
                    btn_speak.setPressed(false);
                    btn_speak.setClickable(false);
                    // 取消录音框
                    layout_record.setVisibility(View.INVISIBLE);
                    // 发送语音消息
//                    sendVoiceMessage(localPath, recordTime);
                    //是为了防止过了录音时间后，会多发一条语音出去的情况。


//                    handler.postDelayed(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            // TODO Auto-generated method stub
//                            btn_speak.setClickable(true);
//                        }
//                    }, 1000);

                } else {

                }
            }
        });
    }



    /**
     * 初始化语音动画资源
     */
    private void initVoiceAnimRes() {

        drawable_Anims = new Drawable[] {
                getResources().getDrawable(R.drawable.chat_icon_voice2),
                getResources().getDrawable(R.drawable.chat_icon_voice3),
                getResources().getDrawable(R.drawable.chat_icon_voice4),
                getResources().getDrawable(R.drawable.chat_icon_voice5),
                getResources().getDrawable(R.drawable.chat_icon_voice6) };
    }



    /**
     * 长按说话
     */
    class VoiceTouchListen implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!CommonUtils.checkSdCard()) {
//                        ShowToast("发送语音需要sdcard支持！");
                        return false;
                    }
                    try {
                        v.setPressed(true);
                        layout_record.setVisibility(View.VISIBLE);
                        tv_voice_tips.setText(getString(R.string.voice_cancel_tips));
                        // 开始录音
                        recordManager.startRecording(userID);
                    } catch (Exception e) {
                    }
                    return true;
                case MotionEvent.ACTION_MOVE: {
                    if (event.getY() < 0) {
                        tv_voice_tips
                                .setText(getString(R.string.voice_cancel_tips));
                        tv_voice_tips.setTextColor(Color.RED);
                    } else {
                        tv_voice_tips.setText(getString(R.string.voice_up_tips));
                        tv_voice_tips.setTextColor(Color.WHITE);
                    }
                    return true;
                }
//                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    v.setPressed(false);
                    layout_record.setVisibility(View.INVISIBLE);
                    try {

                        float aa = event.getY();
                        if (event.getY() < 0) {// 放弃录音
                            recordManager.cancelRecording();
                            BmobLog.i("voice", "放弃发送语音");
                        } else {
                            int recordTime = recordManager.stopRecording();
                            if (recordTime > 1) {
                                // 发送语音文件
                                BmobLog.i("voice", "发送语音");
                                saveAudioFile(recordManager.getRecordFilePath(userID));

                            } else {// 录音时间过短，则提示录音过短的提示
                                layout_record.setVisibility(View.GONE);
//                                showShortToast().show();
                            }
                        }
                    } catch (Exception e) {
                        // TODO: handle exception
                    }
                    return true;
                default:
                    return false;
            }
        }
    }



    public void saveAudioFile(String getVoicePath){



        String[] files = new String[]{getVoicePath};

        Bmob.uploadBatch(context, files, new cn.bmob.v3.listener.UploadBatchListener() {
            @Override
            public void onSuccess(List<BmobFile> list, List<String> list1) {

                if (list.size() > 0) {
                    CommentEntity commentEntity = new CommentEntity();
                    commentEntity.setComment(list.get(0).getUrl());
                    commentEntity.setOwnerMessage(messageEntity);
                    commentEntity.setOwnerUser(currentUser);
                    commentEntity.save(context, new SaveListener() {
                        @Override
                        public void onSuccess() {
                            messageEntity.increment("commentCount");
                            messageEntity.update(context);
                        }

                        @Override
                        public void onFailure(int i, String s) {

                        }
                    });//todo 检测更多动作
                    //todo:save comment to comment list;
                    commentAdapter.addData(commentEntity);
                    // TODO 更新View评论数

                }
            }

            @Override
            public void onProgress(int i, int i1, int i2, int i3) {

            }

            @Override
            public void onError(int i, String s) {

            }
        });

    }









}
