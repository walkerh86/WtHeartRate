package com.wt.heartrate;

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
					mWarningTextView.setVisibility(View.GONE);
					break;
				case MSG_SHOW_RESULT:
					setState(STATE_RESULT);
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.heart_rate_monitor_main);
		
		mWarningTextView = (TextView)findViewById(R.id.heart_rate_warning);
		mHandler.sendEmptyMessageDelayed(MSG_HIDE_WARNING, 2000);
		
		mStateIconView = (ImageView)findViewById(R.id.image);
		mStateIconView.setOnClickListener(mOnClickListener);
		mStateTextView = (TextView)findViewById(R.id.heart_rate_state);
		mStateTextView.setOnClickListener(mOnClickListener);
				
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
				Log.i("hcj", "onSensorChanged value[0]="+arg0.values[0]);
				mCurrRate = (int)arg0.values[0];
			}			
		};
	}
	
	@Override
	public void onResume(){
		super.onResume();
		registerListener();
	}
	
	@Override
	public void onPause(){
		super.onPause();
		unregisterListener();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		unregisterListener();
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
		
		mStateIconView.setBackgroundResource(isDetecting() ? R.drawable.heart_rate_heart_checking_anim : R.drawable.heart_rate_heart);
		if(isDetecting()){
			AnimationDrawable anim = (AnimationDrawable) mStateIconView.getBackground(); 
			anim.start();
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
			setState(STATE_DETECTING);
			mHandler.sendEmptyMessageDelayed(MSG_SHOW_RESULT, 2*2000);
		}
	};
}
