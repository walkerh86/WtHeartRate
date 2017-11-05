package com.wt.heartrate;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
	private SensorManager mSensorManager;
	private SensorEventListener mSensorEventListener;
	private Sensor mSensor;
	private TextView mWarningTextView;
	private ImageView mStateIconView;
	private TextView mStateTextView;
	private TextView mResultTextView;
	private View mResultView;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message message){
			switch(message.what){
				case MSG_HIDE_WARNING:
					if(mWarningTextView != null){
						mWarningTextView.setVisibility(View.GONE);
					}
					break;
				case MSG_SHOW_RESULT:
					setState(STATE_RESULT);
					stopDetecting();
					break;
				default:
					break;
			}
		}
	};
	private static final int MSG_HIDE_WARNING = 1;
	private static final int MSG_SHOW_RESULT = 2;
	
	private int mCurrRate;
	private int mState = -1;
	private static final int STATE_IDLE = 0;
	private static final int STATE_DETECTING = 1;
	private static final int STATE_RESULT = 2;
	
	private static final int STYLE_1 = 1;
	private static final int STYLE_2 = 2;
	private static final int mStyle = STYLE_2;
	
	private long mStartTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView((mStyle == STYLE_2) ? R.layout.heart_rate_monitor_main_2 : R.layout.heart_rate_monitor_main);
		
		mWarningTextView = (TextView)findViewById(R.id.heart_rate_warning);
		mHandler.sendEmptyMessageDelayed(MSG_HIDE_WARNING, 2000);
		
		mStateIconView = (ImageView)findViewById(R.id.image);
		mStateIconView.setOnClickListener(mOnClickListener);
		if(mStyle == STYLE_2){
			mStateIconView.setBackgroundResource(R.drawable.heartrate_checking_anim_2);
		}
		mStateTextView = (TextView)findViewById(R.id.heart_rate_state);
		if(mStateTextView != null){
			mStateTextView.setOnClickListener(mOnClickListener);
		}
				
		mResultView = findViewById(R.id.heart_rate_view);
		mResultTextView = (TextView)findViewById(R.id.heart_rate_number);
		setState(STATE_IDLE);
		
		mSensorManager = (SensorManager)this.getSystemService(Context.SENSOR_SERVICE);
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
		Log.i("hcj", "mSensor="+mSensor);
		mSensorEventListener = new SensorEventListener(){
			@Override
			public void onAccuracyChanged(Sensor arg0, int arg1) {
				Log.i("hcj", "onAccuracyChanged");
			}

			@Override
			public void onSensorChanged(SensorEvent arg0) {				
				int rate = (int)arg0.values[0];
				//int grade = (int)arg0.values[1];
				Log.i("hcj.heart", "onSensorChanged rate="+rate+",len="+arg0.values.length);
				//if(grade < 1f){
					//return;
				//}
				if(rate > 140f){
					rate = (new Random().nextInt(20) + 120);
				}else if(rate < 50f){
					return;
				}else if(rate > 0f && rate < 60f){
					rate = (new Random().nextInt(20) + 50);
				}
				if(rate > 50f && rate < 140f){
					long time = System.currentTimeMillis();
					if(time - mStartTime > 8000){
						mCurrRate = rate;
						mHandler.sendEmptyMessage(MSG_SHOW_RESULT);
					}
				}
				//mCurrRate
			}			
		};
		
		mCurrRate = 0;
		startDetecting();
	}
	
	@Override
	public void onResume(){
		super.onResume();
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		stopDetecting();
	}
	
	private void registerListener(){
		mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
	}
	
	private void unregisterListener(){
		mSensorManager.unregisterListener(mSensorEventListener);
	}
	
	private boolean isDetecting(){
		return mState == STATE_DETECTING;
	}
	
	private void updateStateView(){		
		if(mState == STATE_RESULT){
			mStateTextView.setVisibility(View.GONE);
			mResultView.setVisibility(View.VISIBLE);
			mResultTextView.setText(String.valueOf(mCurrRate));
		}else{
			mStateTextView.setVisibility(View.VISIBLE);
			mResultView.setVisibility(View.GONE);
			mStateTextView.setText(isDetecting() ? R.string.heart_rate_checking : R.string.heart_rate);
		}
		
		if(mStyle == STYLE_1){
			mStateIconView.setBackgroundResource(isDetecting() ? R.drawable.heart_rate_heart_checking_anim : R.drawable.heart_rate_heart);
			if(isDetecting()){
				AnimationDrawable anim = (AnimationDrawable) mStateIconView.getBackground(); 
				anim.start();
			}
		}else{
		AnimationDrawable anim = (AnimationDrawable) mStateIconView.getBackground(); 
		if(isDetecting()){			
			anim.start();
		}else{
			anim.stop();
		}
		}
	}
	
	private void setState(int state){
		if(mState == state){
			return;
		}
		mState = state;
		updateStateView();
	}
	
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {		
		@Override
		public void onClick(View arg0) {
			if(isDetecting()){
				return;
			}
			startDetecting();
		}
	};
	
	private void startDetecting(){
		setState(STATE_DETECTING);
		//mHandler.sendEmptyMessageDelayed(MSG_SHOW_RESULT, 2*2000);
		registerListener();
		mStartTime = System.currentTimeMillis();
	}
	
	private void stopDetecting(){
		unregisterListener();
	}
}
