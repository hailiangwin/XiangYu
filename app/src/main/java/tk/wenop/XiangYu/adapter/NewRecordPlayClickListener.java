package tk.wenop.XiangYu.adapter;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.View;
import android.widget.ImageView;

import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobMsg;
import cn.bmob.im.config.BmobConfig;
import cn.bmob.im.util.BmobLog;
import cn.bmob.im.util.BmobUtils;
import tk.wenop.XiangYu.R;

/**
 * 播放录音文件
 * 
 * @ClassName: NewRecordPlayClickListener
 * @Description: TODO
 * @author smile
 * @date 2014-7-3 上午11:05:06
 */
public class NewRecordPlayClickListener implements View.OnClickListener {

	BmobMsg message;
//	ImageView iv_voice;
	ImageView iv_voice;
	private AnimationDrawable anim = null;
	Context context;
	String currentObjectId = "";
	MediaPlayer mediaPlayer = null;
	public static boolean isPlaying = false;
	public static NewRecordPlayClickListener currentPlayListener = null;
	static BmobMsg currentMsg = null;// 用于区分两个不同语音的播放

	BmobUserManager userManager;

	//custom by llwoll ;
	String voice_path = "";

	public NewRecordPlayClickListener(Context context, BmobMsg msg,
			ImageView voice) {
		this.iv_voice = voice;
		this.message = msg;
		this.context = context;
		currentMsg = msg;
		currentPlayListener = this;
		currentObjectId = BmobUserManager.getInstance(context)
				.getCurrentUserObjectId();
		userManager = BmobUserManager.getInstance(context);
	}

	public NewRecordPlayClickListener(Context context, String msg,
									  ImageView voice) {
		this.iv_voice = voice;
		this.voice_path = msg;
		this.context = context;
//		currentMsg = msg;
		voice_path = msg;
		currentPlayListener = this;
		currentObjectId = BmobUserManager.getInstance(context)
				.getCurrentUserObjectId();
		userManager = BmobUserManager.getInstance(context);
	}

	/**
	 * 播放语音
	 * 
	 * @Title: playVoice
	 * @Description: TODO
	 * @param @param filePath
	 * @param @param isUseSpeaker
	 * @return void
	 * @throws
	 */

	@SuppressWarnings("resource")
	public void startPlayRecord(String filePath, boolean isUseSpeaker) {
		if (!(new File(filePath).exists())) {
			return;
		}
		AudioManager audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		mediaPlayer = new MediaPlayer();
		if (isUseSpeaker) {
			audioManager.setMode(AudioManager.MODE_NORMAL);
			audioManager.setSpeakerphoneOn(true);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		} else {
			audioManager.setSpeakerphoneOn(false);// 关闭扬声器
			audioManager.setMode(AudioManager.MODE_IN_CALL);
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
		}
		
//		while (true) {
//			try {
//				mediaPlayer.reset();
//				FileInputStream fis = new FileInputStream(new File(filePath));
//				mediaPlayer.setDataSource(fis.getFD());
//				mediaPlayer.prepare();
//				break;
//			} catch (IllegalArgumentException e) {
//			} catch (IllegalStateException e) {
//			} catch (IOException e) {
//			}
//		}
//		
//		isPlaying = true;
//		currentMsg = message;
//		mediaPlayer.start();
//		startRecordAnimation();
//		mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//
//			@Override
//			public void onCompletion(MediaPlayer mp) {
//				// TODO Auto-generated method stub
//				stopPlayRecord();
//			}
//
//		});
//        currentPlayListener = this;

		try {
			mediaPlayer.reset();
			// 单独使用此方法会报错播放错误:setDataSourceFD failed.: status=0x80000000
			// mediaPlayer.setDataSource(filePath);
			// 因此采用此方式会避免这种错误
			FileInputStream fis = new FileInputStream(new File(filePath));
			mediaPlayer.setDataSource(fis.getFD());
			mediaPlayer.prepare();
			mediaPlayer.setOnPreparedListener(new OnPreparedListener() {

				@Override
				public void onPrepared(MediaPlayer arg0) {
					// TODO Auto-generated method stub
					isPlaying = true;
					currentMsg = message;
					arg0.start();
					startRecordAnimation();
				}
			});
			mediaPlayer
					.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

						@Override
						public void onCompletion(MediaPlayer mp) {
							// TODO Auto-generated method stub
							stopPlayRecord();
						}

					});
			currentPlayListener = this;
			// isPlaying = true;
			// currentMsg = message;
			// mediaPlayer.start();
			// startRecordAnimation();
		} catch (Exception e) {
			BmobLog.i("播放错误:" + e.getMessage());
		}
	}

	/**
	 * 停止播放
	 * 
	 * @Title: stopPlayRecord
	 * @Description: TODO
	 * @param
	 * @return void
	 * @throws
	 */
	public void stopPlayRecord() {
		stopRecordAnimation();
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		isPlaying = false;
	}

	/**
	 * 开启播放动画
	 */
	private void startRecordAnimation() {

		if (voice_path != "")
		{
			iv_voice.setImageResource(R.anim.anim_chat_voice_left);
            anim = (AnimationDrawable) iv_voice.getDrawable();
            anim.start();
		}

		/*if (message.getBelongId().equals(currentObjectId)) {
			iv_voice.setImageResource(R.anim.anim_chat_voice_right);
		} else {
			iv_voice.setImageResource(R.anim.anim_chat_voice_left);
		}*/

	}

	/**
	 * 停止播放动画
	 */
	private void stopRecordAnimation() {

		if (voice_path!=""){
			iv_voice.setImageResource(R.drawable.voice_right3);
			return;
		}

		/*if (message.getBelongId().equals(currentObjectId)) {
			iv_voice.setImageResource(R.drawable.voice_left3);
		} else {
			iv_voice.setImageResource(R.drawable.voice_right3);
		}*/

		if (anim != null) {
			anim.stop();
		}
	}

	@Override
	public void onClick(View arg0) {
		if (isPlaying) {
			currentPlayListener.stopPlayRecord();
			if (currentMsg != null
					&& currentMsg.hashCode() == message.hashCode()) {
				currentMsg = null;
				return;
			}
		}
		if (voice_path != ""){


			if (voice_path.startsWith("http://file.bmob.cn/")){


				HttpUtils http = new HttpUtils();
				final String localPath = getDownLoadFilePath(voice_path);
				File audioFile = new File(localPath);
				try {
					/*
						加入之前下载过,
					 */
					if (!audioFile.exists()) {
						audioFile.createNewFile();
						http.download(voice_path, localPath, true, true, new RequestCallBack<File>() {
							@Override
							public void onSuccess(ResponseInfo<File> responseInfo) {
								startPlayRecord(localPath, true);
							}

							@Override
							public void onFailure(HttpException e, String s) {
//						startPlayRecord(localPath, true);
							}
						});

					}else {
						startPlayRecord(localPath, true);
					}


				} catch (IOException e) {
				}

			}else {
				startPlayRecord(voice_path, true);
			}

			return;
		}


		BmobLog.i("voice", "点击事件");
		if (message.getBelongId().equals(currentObjectId)) {// 如果是自己发送的语音消息，则播放本地地址
			String localPath = message.getContent().split("&")[0];
			startPlayRecord(localPath, true);
		} else {// 如果是收到的消息，则需要先下载后播放
			String localPath = getDownLoadFilePath(message);
			BmobLog.i("voice", "收到的语音存储的地址:" + localPath);
			startPlayRecord(localPath, true);
		}
	}

	/*
		custom by llwoll
		根据服务器路径返回的路径，计算本地保存路径
	 */
	public String getDownLoadFilePath(String remoteUrl) {

		String[] urls = remoteUrl.split("/");
		int len = urls.length;
		String name = urls[len -1];


		String accountDir = BmobUtils.string2MD5(userManager
				.getCurrentUserObjectId());
		File dir = new File(BmobConfig.BMOB_VOICE_DIR + File.separator
				+ accountDir + File.separator + accountDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}


		String path = dir.getAbsolutePath() + File.separator+ name;
//		// 在当前用户的目录下面存放录音文件
//		File audioFile = new File(dir.getAbsolutePath() + File.separator
//				+ name);
//		try {
//			if (!audioFile.exists()) {
//				audioFile.createNewFile();
//			}
//		} catch (IOException e) {
//		}
		return path;
	}


	public String getDownLoadFilePath(BmobMsg msg) {
		String accountDir = BmobUtils.string2MD5(userManager
				.getCurrentUserObjectId());
		File dir = new File(BmobConfig.BMOB_VOICE_DIR + File.separator
				+ accountDir + File.separator + msg.getBelongId());
		if (!dir.exists()) {
			dir.mkdirs();
		}
		// 在当前用户的目录下面存放录音文件
		File audioFile = new File(dir.getAbsolutePath() + File.separator
				+ msg.getMsgTime() + ".amr");

		return audioFile.getAbsolutePath();
	}

}