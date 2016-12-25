package mobi.boilr.boilr.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import mobi.boilr.boilr.R;
import mobi.boilr.boilr.services.NotificationService;
import mobi.boilr.boilr.utils.Languager;
import mobi.boilr.boilr.utils.Notifications;
import mobi.boilr.boilr.utils.Themer;

/**
 * Alarm activity that pops up a visible indicator when an alarm goes off.
 */
public class NotificationActivity extends Activity {

	private int mAlarmID;
	private boolean stopNotify = true, keepMonitoring = false;
	public static final String FINISH_ACTION = "mobi.boilr.boilr.action.finish";
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Themer.applyTheme(this);
		Languager.setLanguage(this);
		registerReceiver(mBroadcastReceiver, new IntentFilter(FINISH_ACTION));
		setTitle(getResources().getString(R.string.boilr_alarm));
		mAlarmID = getIntent().getIntExtra("alarmID", Integer.MIN_VALUE);
		String firingReason = getIntent().getStringExtra("firingReason");
		boolean canKeepMonitoring = getIntent().getBooleanExtra("canKeepMonitoring", false);
		boolean isDirectionUp = getIntent().getBooleanExtra("isDirectionUp", true);
		final Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
				WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
				WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		final LayoutInflater inflater = LayoutInflater.from(this);
		View view = inflater.inflate(R.layout.alarm_alert, null);
		view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		((TextView) view.findViewById(R.id.firing_reason)).setText(firingReason);
		ImageView arrowView = (ImageView) view.findViewById(R.id.arrow);
		if(isDirectionUp)
			arrowView.setImageBitmap(Notifications.sBigUpArrowBitmap);
		else
			arrowView.setImageBitmap(Notifications.sBigDownArrowBitmap);
		if(!canKeepMonitoring) {
			view.findViewById(R.id.resume_wrapper).setVisibility(View.GONE);
		}
		int orient = getResources().getConfiguration().orientation;
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.text_wrapper);
		if(orient == Configuration.ORIENTATION_LANDSCAPE) {
			layout.setOrientation(LinearLayout.HORIZONTAL);
			layout.setGravity(Gravity.CENTER);
		}
		setContentView(view);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
		if(stopNotify)
			NotificationService.stopNotify(this, mAlarmID, keepMonitoring);
	}

	@Override
	public void onBackPressed() {
		// Don't allow back to dismiss.
	}

	@Override
	public void onUserLeaveHint() {
		super.onUserLeaveHint();
		// User clicked Home or Power button.
		finish();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		/*
		 * This boolean detects that the Activity will be re-created soon. This
		 * happens when screen orientation changes. We must keep the alarm
		 * ringing on such cases.
		 */
		stopNotify = false;
	}

	public void onOffClicked(View v) {
		finish();
	}

	public void onResumeClicked(View v) {
		keepMonitoring = true;
		finish();
	}
}
