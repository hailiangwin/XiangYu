package tk.wenop.XiangYu.adapter.custom;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import tk.wenop.XiangYu.R;
import tk.wenop.XiangYu.adapter.NewRecordPlayClickListener;
import tk.wenop.XiangYu.bean.MessageEntity;
import tk.wenop.XiangYu.ui.wenui.CommentActivity;
import tk.wenop.XiangYu.ui.wenui.PeopleDetailActivity;

//import tk.wenop.testapp.Overview.MainScreenOverviewItem;
//import tk.wenop.testapp.R;
//import tk.wenop.testapp.UI.CommentActivity;
//import tk.wenop.testapp.UI.PeopleDetailActivity;

/**
 * Created by wenop_000 on 2015/10/12.
 */
public class MainScreenChatAdapter extends RecyclerView.Adapter<MainScreenChatAdapter.ViewHolder> {


    private List<MessageEntity> mDataset = new ArrayList<>();
    ImageLoader imageLoader;
//    private ArrayList<MainScreenOverviewItem> mDataset;
    protected Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;

        public TextView  mNickName;
        public TextView  mAudioTimeSec;
        public ImageView mAvatar;
        public ImageView mContentPhoto;
        public RelativeLayout audio_msg_bubble;
        public ImageView audio_animation;
        public TextView  mLocation;
        public TextView  mCommentCount;
        public TextView  mTime;



        public ViewHolder(View v) {
            super(v);
            mView = v;
            mNickName      = (TextView)  v.findViewById(R.id.textView_nickName        );
            mAudioTimeSec  = (TextView)  v.findViewById(R.id.textView_audioLength     );
            mAvatar        = (ImageView) v.findViewById(R.id.imageView_avatar        );
            mContentPhoto  = (ImageView) v.findViewById(R.id.imageView_contentPhoto  );
            //按气泡播放语音
            audio_msg_bubble = (RelativeLayout) v.findViewById(R.id.audio_msg_bubble);
            //声音按钮
            audio_animation = (ImageView) v.findViewById(R.id.imageView2);

            mLocation      = (TextView)  v.findViewById(R.id.textView_location        );
            mCommentCount  = (TextView)  v.findViewById(R.id.textView_commentCount    );
            mTime          = (TextView)  v.findViewById(R.id.textView_time            );


        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public MainScreenChatAdapter(Context context,  List<MessageEntity> myDataset) {
        mDataset.addAll(myDataset);
        mContext = context;
        imageLoader = ImageLoader.getInstance();
    }


    public void putDataSet(List<MessageEntity> myDataset){
        myDataset.clear();
        myDataset.addAll(myDataset);
        notifyDataSetChanged();
    }


    // Create new views (invoked by the layout manager)
    @Override
    public MainScreenChatAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_received_list__main_screen_chat, parent, false);

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: onClick在这里
            }
        });

        return new ViewHolder(v);
    }

    // 点击头像要跳转到用户详情页
    private final View.OnClickListener onAvatarClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(mContext, PeopleDetailActivity.class);
//            TODO 把用户信息传过去?
//            intent.putExtra("BAR_CODE_RESULT", result);
            mContext.startActivity(intent);
        }
    };

    // 点击评论按钮
    private final View.OnClickListener onCommentClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(mContext, CommentActivity.class);
//            TODO 把相关信息传过去?
//            intent.putExtra("BAR_CODE_RESULT", result);
            mContext.startActivity(intent);
        }
    };



    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        MessageEntity data = mDataset.get(position);
//        holder.mAvatar.setImageResource();
        if (data.getOwnerUser()!=null){
            holder.mNickName.setText(data.getOwnerUser().getNick());
        }


        holder.mAvatar.setOnClickListener(onAvatarClickListener);

        imageLoader.displayImage("http://file.bmob.cn/" + data.getImage(), holder.mContentPhoto);
        String path = "http://file.bmob.cn/" + data.getAudio();
        holder.audio_msg_bubble.setOnClickListener(new NewRecordPlayClickListener(mContext,path,holder.audio_animation));


//        holder.audio.setOnClickListener(new NewRecordPlayClickListener(context,path, messageHolder.audio));
        //评论按钮
        holder.mView.findViewById(R.id.group_comment)
                .setOnClickListener(onCommentClickListener);
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}