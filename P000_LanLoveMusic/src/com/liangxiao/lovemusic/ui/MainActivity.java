package com.liangxiao.lovemusic.ui;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import cn.jpush.android.api.JPushInterface;

import com.liangxiao.lovemusic.R;
import com.liangxiao.lovemusic.data.Const;
import com.liangxiao.lovemusic.model.IAlertDialogButtonListener;
import com.liangxiao.lovemusic.model.IWordButtonClickListener;
import com.liangxiao.lovemusic.model.Song;
import com.liangxiao.lovemusic.model.WordButton;
import com.liangxiao.lovemusic.myui.MyGridView;
import com.liangxiao.lovemusic.util.MyPlayer;
import com.liangxiao.lovemusic.util.Util;
import com.liangxiao.lovemusic.util.WeChatUtil;
import com.liangxiao.lovemusic.wxapis.WXEntryActivity;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;

public class MainActivity extends Activity implements IWordButtonClickListener {

	public static final String KEY_EXTRAS = "extras";
	public final static int STATUS_ANSWER_RIGHT = 1;// ����ȷ
	public final static int STATUS_ANSWER_WRONG = 2;// �𰸴���
	public final static int STATUS_ANSWER_PART = 3;// �𰸲�����
	public final static int SPASH_TIMES = 6; // ��˸����
	public final static int ID_DIALOG_DELETE = 1; // ɾ�����30��
	public final static int ID_DIALOG_TIPS = 2; // ��ʾ���90��
	public final static int ID_DIALOG_LACK = 3; // ��Ҳ���
	public final static int ID_DIALOG_SHARE = 4; // ����΢��
	// ��Ƭ��ض���
	Animation mPanAnimation;
	LinearInterpolator mPanLinearInterpolator;
	Animation mBarInAnimation;
	LinearInterpolator mBarInLinearInterpolator;
	Animation mBarOutAnimation;
	LinearInterpolator mBarOutLinearInterpolator;
	// ��Ƭ�ؼ�
	ImageView img1;
	// ���˿ؼ�
	ImageView img2;
	// ��ǰ������
	TextView mCurrentStagePassView;
	TextView mCurrentStageView;
	TextView mCurrentSongNamePassView;
	// play
	ImageButton btn_play_start;
	// WeChat
	ImageButton btn_Wechat;
	// ��ǰ�����Ƿ�����
	Boolean mIsRunning = false;
	// ���ֿ�����
	ArrayList<WordButton> mAddWords;
	ArrayList<WordButton> mBtnSelectWords;
	MyGridView mMyGridView;
	// ��ѡ�����ֿ�UI����
	LinearLayout mViewWordsContainer;
	// ��ǰ�ĸ���
	Song mCurrentSong;
	// ��ǰ�ĵڼ��ص�����
	int mCurrentStageIndex = -1;
	// ���ؼ���xml
	View mPassView;
	// ͨ�ؽ������xml
	View mAllPassView;
	// ��ǰ��ҵ�����
	int mCurrentCoins = Const.TOTAL_COINS;
	// ��ҵ�TextView
	TextView mTextViewCurrentCoins;

	// ���صĽ��WeChat result
	String result;
	EditText editText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// ��ȡ��Ϸ������
		int[] datas = Util.ReadData(MainActivity.this);
		mCurrentStageIndex = datas[Const.INDEX_READ_DATA_STAGE];
		mCurrentCoins = datas[Const.INDEX_READ_DATA_COINS];

		mTextViewCurrentCoins = (TextView) findViewById(R.id.txt_bar_coins);
		mTextViewCurrentCoins.setText(mCurrentCoins + "");
		img1 = (ImageView) findViewById(R.id.img1);
		img2 = (ImageView) findViewById(R.id.img2);
		mMyGridView = (MyGridView) findViewById(R.id.gridview);
		mMyGridView.registOnWordButtonClick(this);
		mViewWordsContainer = (LinearLayout) findViewById(R.id.word_select_container);
		btn_play_start = (ImageButton) findViewById(R.id.btn_play_start);
		btn_Wechat = (ImageButton) findViewById(R.id.btn_Wechat);
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View dialog_view = layoutInflater.inflate(R.layout.dialog_view_share,
				null);
		editText = (EditText) dialog_view.findViewById(R.id.txt_dailog_message);
		editText.setText("�����ֲ¸�������Ҷ����²�����˭����ַ��http://zhushou.360.cn/detail/index/soft_id/1767844?recrefer=SE_D_��С��");
		// result = editText.getText().toString();
		// ���ض���
		init_animation();
		// ע��΢��
		WeChatUtil.getInstance(MainActivity.this);

		// ��ʼ����Ϸ���ֵ����ݲ���
		initCurrentStageData();
		// ����ɾ����ѡ���ֿ�30�ֵĲ���
		handleDeleteWord();
		// ������ʾ���ֿ��������¼�����
		handleTipAnswer();
		btn_play_start.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				handlePlayButton();
			}
		});
		// WeChat
		btn_Wechat.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// ��ʾ�Ի���
				// showConfirmDialog(ID_DIALOG_SHARE);
				// WeChat
				String a = "�����ֲ¸�������Ҷ����²�����˭����ַ��http://zhushou.360.cn/detail/index/soft_id/1767844?recrefer=SE_D_��С��";
				WeChatUtil.sentRequest(a);
				// Handler handler = new Handler();
				// handler.postDelayed(new Runnable() {
				//
				// @Override
				// public void run() {
				// Toast.makeText(MainActivity.this, "����ɹ�", 2).show();
				// }
				// }, 5000);
			}
		});
		// Intent intent = getIntent();
		// result = intent.getStringExtra("result");
		// Toast.makeText(this, result, Toast.LENGTH_LONG);
	}

	/**
	 * ���Ű�ť
	 */
	protected void handlePlayButton() {
		if (img2 != null) {
			if (!mIsRunning) {
				mIsRunning = true;
				// ��ʼ���˽���Ķ�������
				img2.startAnimation(mBarInAnimation);
				btn_play_start.setVisibility(View.INVISIBLE);

				// ��������
				MyPlayer.playSong(MainActivity.this,
						mCurrentSong.getSongFileName());

			}
		}

	}

	@Override
	protected void onResume() {
		JPushInterface.onResume(this);
		super.onResume();
	}

	@Override
	public void onPause() {
		JPushInterface.onPause(this);
		// ������Ϸ����
		Util.saveData(MainActivity.this, mCurrentStageIndex - 1, mCurrentCoins);
		img1.clearAnimation();
		// ��ͣ����
		MyPlayer.stopSong(MainActivity.this);
		super.onPause();
	}

	/**
	 * ������ʾ���ֿ��������¼�����
	 */
	private void handleTipAnswer() {
		ImageButton btn_tip_answer = (ImageButton) findViewById(R.id.btn_tip_answer);
		btn_tip_answer.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// ��ʾ�Ի���
				showConfirmDialog(ID_DIALOG_TIPS);

			}
		});
	}

	/**
	 * ��ʾһ���ֿ�90��
	 */
	private void tipAnswer() {
		boolean tipWord = false;
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			if (mBtnSelectWords.get(i).mWordString.length() == 0) {

				tipWord = true;
				// ���ٽ������
				if (!handleCoins(-getTipWordCoins())) {
					// ��Ҳ���
					showConfirmDialog(ID_DIALOG_LACK);
					return;
				}
				// ���ݵ�ǰ�Ĵ𰸿�����ѡ���Ӧ�����ֲ�����
				onWordButtonClick(findIsAnswerWord(i));
				break;
			}
		}
		// û���ҵ��������Ĵ�
		if (!tipWord) {
			// ��˸������ʾ�û�
			sparkTheWords();
		}
	}

	/**
	 * �ҵ�һ��������
	 * 
	 * @param i
	 *            ��ǰ��Ҫ����𰸿������
	 * @return
	 */
	private WordButton findIsAnswerWord(int index) {
		WordButton buf = null;
		for (int i = 0; i < MyGridView.COUNTS_WORDS; i++) {
			buf = mAddWords.get(i);
			if (buf.mWordString.equals(""
					+ mCurrentSong.getNameCharacters()[index])) {
				return buf;
			}
		}
		return null;
	}

	/**
	 * ����ɾ����ѡ���ֿ�30�ֵĲ���
	 */
	private void handleDeleteWord() {
		ImageButton btn_delete_word = (ImageButton) findViewById(R.id.btn_delete_word);
		btn_delete_word.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// ��ʾ�Ի���
				showConfirmDialog(ID_DIALOG_DELETE);

			}
		});
	}

	/**
	 * �Զ����AlertDialog�¼���Ӧ ɾ��30�ֲ���
	 */
	/**
	 * ��30��
	 */
	private IAlertDialogButtonListener mBtnOkDeleteWordListener = new IAlertDialogButtonListener() {

		@Override
		public void onClick() {
			// ɾ��һ���ֿ�30��
			deleteoneword();
		}
	};
	/**
	 * ��90��
	 */
	private IAlertDialogButtonListener mBtnOkTipAnswerListener = new IAlertDialogButtonListener() {

		@Override
		public void onClick() {
			// ��ʾһ���ֿ�90��
			tipAnswer();
		}
	};
	/**
	 * ����΢��
	 */
	private IAlertDialogButtonListener shareListener = new IAlertDialogButtonListener() {

		@Override
		public void onClick() {
			// ִ���¼�
			// WXEntryActivity.sentRequest(editText.getText().toString());
			String text = editText.getText().toString();
			// ��ʼ��һ��WXTextObject����
			WXTextObject textObj = new WXTextObject();
			textObj.text = text;

			// ��WXTextObject�����ʼ��һ��WXMediaMessage����
			WXMediaMessage msg = new WXMediaMessage();
			msg.mediaObject = textObj;
			// �����ı����͵���Ϣʱ��title�ֶβ�������
			// msg.title = "Will be ignored";
			msg.description = text;

			// ����һ��Req
			SendMessageToWX.Req req = new SendMessageToWX.Req();
			req.transaction = buildTransaction("text"); // transaction�ֶ�����Ψһ��ʶһ������
			req.message = msg;
			req.scene = SendMessageToWX.Req.WXSceneTimeline;
			// req.scene = isTimelineCb.isChecked() ?
			// SendMessageToWX.Req.WXSceneTimeline
			// : SendMessageToWX.Req.WXSceneSession;

			// ����api�ӿڷ������ݵ�΢��
			WXEntryActivity.mApi.sendReq(req);
			// finish();
		}
	};

	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis())
				: type + System.currentTimeMillis();
	}

	/**
	 * ��Ҳ���
	 */
	private IAlertDialogButtonListener mBtnLackCoinsListener = new IAlertDialogButtonListener() {

		@Override
		public void onClick() {
			// ִ���¼�
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			Uri content_url = Uri.parse("http://liangxiao.blog.51cto.com/");
			intent.setData(content_url);
			startActivity(intent);
			MainActivity.this.finish();
		}
	};

	/**
	 * ��ʾdialog
	 * 
	 * @param id
	 */
	private void showConfirmDialog(int id) {
		switch (id) {
		case ID_DIALOG_DELETE:
			Util.showDialog(MainActivity.this, "ȷ������" + getDeleteWordCoins()
					+ "���ɾ��һ������𰸣�", mBtnOkDeleteWordListener);
			break;
		case ID_DIALOG_TIPS:
			Util.showDialog(MainActivity.this, "ȷ������" + getTipWordCoins()
					+ "�����ʾһ����ȷ�𰸣�", mBtnOkTipAnswerListener);
			break;
		case ID_DIALOG_LACK:
			Util.showDialog(MainActivity.this, "��Ҳ��������ֵ",
					mBtnLackCoinsListener);
			break;

		case ID_DIALOG_SHARE:
			Util.showDialog_share(MainActivity.this, editText.getText()
					.toString(), shareListener);
			break;
		default:
			break;
		}
	}

	/**
	 * ɾ��һ���ֿ�30��
	 */
	private void deleteoneword() {
		// ���ٽ��
		if (!handleCoins(-getDeleteWordCoins())) {
			// ��Ҳ�����dailog
			showConfirmDialog(ID_DIALOG_LACK);
			return;
		}
		// ������������������ò��ɼ�������30��
		setButtonVisiable(findNotAnswerWord(), View.INVISIBLE);

	}

	/**
	 * �������ļ����ȡɾ��������Ҫ�õĽ��
	 */
	private int getDeleteWordCoins() {
		return this.getResources().getInteger(R.integer.pay_delete_word);
	}

	/**
	 * �������ļ����ȡ��ʾ������Ҫ�õĽ��
	 */
	private int getTipWordCoins() {
		return this.getResources().getInteger(R.integer.pay_tip_answer);
	}

	/**
	 * �������ļ����ȡ���ز�����Ҫ�õĽ��
	 */
	private int getAddWordCoins() {
		return this.getResources().getInteger(R.integer.pay_add_word);
	}

	/**
	 * Ѱ��һ���������
	 * 
	 * @return ���������ȷ�����22�������е�һ��
	 */
	private WordButton findNotAnswerWord() {
		Random random = new Random();
		WordButton buf = null;
		while (true) {
			int index = random.nextInt(MyGridView.COUNTS_WORDS);
			buf = mAddWords.get(index);
			if (buf.mIsVisiable && !isTheAnswerWord(buf)) {
				return buf;
			}
		}

	}

	/**
	 * �ж�24������������Ƿ�����ȷ�����ֲ���
	 */
	private boolean isTheAnswerWord(WordButton word) {
		boolean result = false;
		for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
			if (word.mWordString.equals(""
					+ mCurrentSong.getNameCharacters()[i])) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * ����/���ٽ�ҵ��������ִ�ֵ����
	 */
	private boolean handleCoins(int data) {
		// �жϵ�ǰ�ܵĽ�������Ƿ�ɱ�����
		if (mCurrentCoins + data >= 0) {
			mCurrentCoins += data;
			mTextViewCurrentCoins.setText(mCurrentCoins + "");
			return true;
		} else {
			return false;
		}
	}

	public void onWordButtonClick(WordButton wordButton) {
		// Toast.makeText(this, wordButton.mViewButton+"",
		// Toast.LENGTH_SHORT).show();
		setSelectWord(wordButton);

		// ��ȡ�𰸵�״̬
		int check = checkTheAnswer();

		if (check == STATUS_ANSWER_RIGHT) {
			// ������ȷ��xml������ȷ
			handlePassEvent();
		} else if (check == STATUS_ANSWER_WRONG) {
			// ��˸��������
			sparkTheWords();
		} else if (check == STATUS_ANSWER_PART) {
			for (int i = 0; i < mBtnSelectWords.size(); i++) {
				// ���ô𰸿�������Ϊ��ɫ
				mBtnSelectWords.get(i).mViewButton.setTextColor(Color.WHITE);

			}
		}
	}

	/**
	 * ������ȷ��xml������ȷ
	 */
	private void handlePassEvent() {
		// ��ʾ���ؽ���
		mPassView = (LinearLayout) this.findViewById(R.id.pass_layout);
		mPassView.setVisibility(View.VISIBLE);
		// �չض���
		img1.clearAnimation();
		// ֹͣ��������
		MyPlayer.stopSong(MainActivity.this);
		// �ӽ��
		handleCoins(+getAddWordCoins());
		// ���Ž������
		MyPlayer.playTone(MainActivity.this, MyPlayer.INDEX_STONE_COIN);
		// ��ǰ�ص�����
		mCurrentStagePassView = (TextView) findViewById(R.id.guanka);
		if (mCurrentStagePassView != null) {
			mCurrentStagePassView.setText((mCurrentStageIndex + 1) + "");
		}
		// ��ʾ����������
		mCurrentSongNamePassView = (TextView) findViewById(R.id.txt_song_name);
		if (mCurrentSongNamePassView != null) {
			mCurrentSongNamePassView.setText(mCurrentSong.getSongName());
		}
		// ��һ�ذ�ť
		ImageButton btn_next = (ImageButton) findViewById(R.id.btn_next);
		btn_next.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (judegAppPassed()) {
					// ���뵽ͨ�ؽ���
					Util.startActivity(MainActivity.this, AllPassView.class);

				} else {
					// ��ʼ�µ�һ��
					mPassView.setVisibility(View.GONE);

					// ���عؿ�����
					initCurrentStageData();

				}
			}
		});
		// share��ť
		ImageButton btn_share = (ImageButton) findViewById(R.id.btn_share);
		btn_share.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mPassView.setVisibility(View.GONE);
				String a = "�����ֲ¸�������Ҷ����²�����˭����ַ��http://zhushou.360.cn/detail/index/soft_id/1767844?recrefer=SE_D_��С��";
				WeChatUtil.sentRequest(a);

			}
		});
	}

	/**
	 * �ж��Ƿ�ͨ��
	 */
	private boolean judegAppPassed() {
		return (mCurrentStageIndex == Const.SONG_INFO.length - 1);
	}

	/**
	 * ��˸�������Ѳ���
	 */
	private void sparkTheWords() {
		final Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			boolean mChange = false;
			int mSpardTimes = 0;

			@Override
			public void run() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (++mSpardTimes > SPASH_TIMES) {
							// ����6�ξͲ���˸
							timer.cancel();
							return;
						}

						// ִ����˸�߼���������ʾ��ɫ�Ͱ�ɫ����
						for (int i = 0; i < mBtnSelectWords.size(); i++) {
							mBtnSelectWords.get(i).mViewButton
									.setTextColor(mChange ? Color.RED
											: Color.WHITE);
						}
						mChange = !mChange;
					}
				});

			}
		};

		timer.schedule(task, 1, 150);
	}

	/**
	 * ��ȡ�𰸵�״̬
	 * 
	 * @return
	 */
	private int checkTheAnswer() {
		// �ȼ�鳤��
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			if (mBtnSelectWords.get(i).mWordString.length() == 0) {
				return STATUS_ANSWER_PART;
			}
		}
		// ��������������
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			sb.append(mBtnSelectWords.get(i).mWordString);
		}

		int checkID = sb.toString().equals(mCurrentSong.getSongName()) ? STATUS_ANSWER_RIGHT
				: STATUS_ANSWER_WRONG;
		return checkID;
	}

	/**
	 * ���ô��������ֲ���
	 * 
	 * @param wordButton
	 */
	private void setSelectWord(WordButton wordButton) {
		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			if (mBtnSelectWords.get(i).mWordString.length() == 0) {
				// ���ô����ֿ�����ݼ��ɼ���
				mBtnSelectWords.get(i).mWordString = wordButton.mWordString;
				mBtnSelectWords.get(i).mViewButton
						.setText(wordButton.mWordString);
				mBtnSelectWords.get(i).mIsVisiable = true;
				// ��¼����
				mBtnSelectWords.get(i).mIndex = wordButton.mIndex;
				// ����GridView����Ĵ�ѡ���ֿɼ���
				setButtonVisiable(wordButton, View.INVISIBLE);
				break;
			} else {
				// wucaozuo
			}
		}
	}

	/**
	 * ����GridView����Ĵ�ѡ���ֿɼ���
	 * 
	 * @param wordButton
	 * @param invisible
	 */
	private void setButtonVisiable(WordButton wordButton, int invisible) {
		wordButton.mViewButton.setVisibility(invisible);
		wordButton.mIsVisiable = (invisible == View.VISIBLE) ? true : false;

	}

	private void init_animation() {
		// ֹͣ���ŵ�ʱ�򲦸˵Ķ���Ч������
		mPanAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate);
		mPanLinearInterpolator = new LinearInterpolator();
		mPanAnimation.setInterpolator(mPanLinearInterpolator);
		mPanAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {

			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				// ��ʼ�����˳���ʱ�򶯻�Ч��
				img2.startAnimation(mBarOutAnimation);
			}
		});
		// ��ʼ��Ƭ��������
		mBarInAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_45);
		mBarInLinearInterpolator = new LinearInterpolator();
		mBarInAnimation.setFillAfter(true);
		mBarInAnimation.setInterpolator(mBarInLinearInterpolator);
		mBarInAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {

			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				// ��ʼimg1��Ƭ����
				img1.startAnimation(mPanAnimation);
			}
		});

		// ���׶�����������
		mBarOutAnimation = AnimationUtils.loadAnimation(this,
				R.anim.rotate_d_45);
		mBarOutLinearInterpolator = new LinearInterpolator();
		mBarOutAnimation.setFillAfter(true);
		mBarOutAnimation.setInterpolator(mBarOutLinearInterpolator);
		mBarOutAnimation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation arg0) {

			}

			@Override
			public void onAnimationRepeat(Animation arg0) {

			}

			@Override
			public void onAnimationEnd(Animation arg0) {
				Log.i("tag", "��������");
				mIsRunning = false;
				btn_play_start.setVisibility(View.VISIBLE);
			}
		});
	}

	/**
	 * ��ǰ�ؿ���������Ϣ����
	 */
	private void initCurrentStageData() {
		// ��ȡ��ǰ�ؿ��ĸ�����Ϣ
		mCurrentSong = loadStageSongInfo(++mCurrentStageIndex);// ��0��ʼ���������õ�һ����������,ru{"__00000.m4a","����"}

		// ��ʼ����ѡ���
		mBtnSelectWords = initWordSelect();
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		// �����һ����
		mViewWordsContainer.removeAllViews();

		for (int i = 0; i < mBtnSelectWords.size(); i++) {
			mViewWordsContainer.addView(mBtnSelectWords.get(i).mViewButton,
					params);
		}
		// ��ǰ�Ĺ�����
		mCurrentStageView = (TextView) findViewById(R.id.txt_passview_mainactivity);
		if (mCurrentStageView != null) {
			mCurrentStageView.setText((mCurrentStageIndex + 1) + "");
		}

		// ��ȡ����
		mAddWords = initAllWord();
		// �������� - MyGridView
		mMyGridView.updateData(mAddWords);
		// һ��ʼ�Ͳ���
		handlePlayButton();
	}

	// ��ȡ��ǰ�ؿ��ĸ�����Ϣ,��Song���� ����
	private Song loadStageSongInfo(int stageIndex) {
		Song song = new Song();
		String[] stage = Const.SONG_INFO[stageIndex];
		song.setSongFileName(stage[Const.INDEX_SONG_FILENAME]);
		song.setSongName(stage[Const.INDEX_SONG_NAME]);

		return song;
	}

	/**
	 * �������е����֣���̬���ɵ�����+��������=24
	 */
	private String[] generateWord() {
		Random random = new Random();
		String[] words = new String[MyGridView.COUNTS_WORDS];
		// ����������ֲ���
		// String a = mCurrentSong.getNameLength() + "";
		for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
			words[i] = mCurrentSong.getNameCharacters()[i] + "";
		}
		// ��ȡ������ֲ���
		for (int i = mCurrentSong.getNameLength(); i < MyGridView.COUNTS_WORDS; i++) {
			words[i] = Util.getRandomChar() + "";
		}

		// ��������˳�����ȴ�����Ԫ�������ѡȡһ�����һ��Ԫ�ؽ��н�����
		// Ȼ���ڵڶ���֮��ѡ��һ��Ԫ����ڶ���������֪�����һ��Ԫ�ء�
		// �����ܹ�ȷ��ÿ��Ԫ����ÿ��λ�õĸ��ʶ���1/n��

		for (int i = MyGridView.COUNTS_WORDS - 1; i >= 0; i--) {
			int index = random.nextInt(i + 1);// 23��������һ�����������22����������һ��������

			String buf = words[index];
			words[index] = words[i];
			words[i] = buf;

		}

		return words;
	}

	/**
	 * ��ʼ����ѡ���ֿ�24
	 */
	private ArrayList<WordButton> initAllWord() {
		ArrayList<WordButton> data = new ArrayList<WordButton>();

		// ������д�ѡ����
		String[] words = generateWord();
		// .........

		for (int i = 0; i < MyGridView.COUNTS_WORDS; i++) {
			WordButton button = new WordButton();

			button.mWordString = words[i];

			data.add(button);
		}

		return data;
	}

	/**
	 * ��ʼ����ѡ������ֿ�
	 * 
	 * @return
	 */
	private ArrayList<WordButton> initWordSelect() {
		ArrayList<WordButton> data = new ArrayList<WordButton>();
		for (int i = 0; i < mCurrentSong.getNameLength(); i++) {
			View view = Util.getView(MainActivity.this,
					R.layout.name_selector_item);
			final WordButton holder = new WordButton();
			holder.mViewButton = (Button) view.findViewById(R.id.item_btn);
			holder.mViewButton.setTextColor(Color.WHITE);
			holder.mViewButton.setText("");
			holder.mIsVisiable = false;
			holder.mViewButton.setBackgroundResource(R.drawable.game_wordblank);
			holder.mViewButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// ��յ�ǰ��һ���֣�Ȼ��ԭgridview��mcurrentindex�Ŀɼ���
					clearSelfVisiableGirdView(holder);
				}
			});
			data.add(holder);
		}
		return data;
	}

	private void clearSelfVisiableGirdView(WordButton wordButton) {
		wordButton.mViewButton.setText("");
		wordButton.mWordString = "";
		wordButton.mIsVisiable = false;

		// ����gridview�Ŀɼ���
		setButtonVisiable(mAddWords.get(wordButton.mIndex), View.VISIBLE);
	}

}
