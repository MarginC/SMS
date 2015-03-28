package com.margin.sms;

import java.util.ArrayList;

import android.support.v7.app.ActionBarActivity;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	private EditText et_number;
	private EditText et_content;

	private BroadcastReceiver br_send_sms, br_delivered_sms;

	private static String SMS_SEND_ACTIOIN = "SMS_SEND_ACTIOIN";
	private static String SMS_DELIVERED_ACTION = "SMS_DELIVERED_ACTION";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		et_number = (EditText) findViewById(R.id.et_number);
		et_content= (EditText) findViewById(R.id.et_content);

		Button bt_send = (Button) findViewById(R.id.bt_send);
		bt_send.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		IntentFilter intentFilter;

		intentFilter = new IntentFilter(SMS_SEND_ACTIOIN);
		br_send_sms = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(SMS_SEND_ACTIOIN)) {
					switch (getResultCode()) {
					case Activity.RESULT_OK:
						Toast.makeText(MainActivity.this,
								getResources().getText(R.string.send_success), Toast.LENGTH_SHORT).show();
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						Toast.makeText(MainActivity.this,
								getResources().getText(R.string.send_failed), Toast.LENGTH_SHORT).show();
						break;
					default:
						break;
					}
				}
			}
		};
		registerReceiver(br_send_sms, intentFilter);

		intentFilter = new IntentFilter(SMS_DELIVERED_ACTION);
		br_delivered_sms = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals(SMS_DELIVERED_ACTION)) {
					switch (getResultCode()) {
					case Activity.RESULT_OK:
						Toast.makeText(MainActivity.this,
								getResources().getText(R.string.delivered_success), Toast.LENGTH_LONG).show();
						break;
					case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
						Toast.makeText(MainActivity.this,
								getResources().getText(R.string.delivered_failed), Toast.LENGTH_LONG).show();
						break;
					default:
						break;
					}
				}
			}
		};
		registerReceiver(br_delivered_sms, intentFilter);
		super.onResume();
	}

	@Override
	public void onPause() {
		unregisterReceiver(br_send_sms);
		unregisterReceiver(br_delivered_sms);
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bt_send:
			String number = et_number.getText().toString().trim();
			String content = et_content.getText().toString().trim();
			if(TextUtils.isEmpty(number)) {
				Toast.makeText(getApplicationContext(),
						getResources().getText(R.string.empty_number), Toast.LENGTH_LONG).show();
			} else if(TextUtils.isEmpty(content)) {
				Toast.makeText(getApplicationContext(),
						getResources().getText(R.string.empty_content), Toast.LENGTH_LONG).show();
			} else {
				new Thread(new SmsThread(number, content)).start();
				Toast.makeText(MainActivity.this,
						getResources().getText(R.string.sending), Toast.LENGTH_LONG).show();
			}
			break;
		default:
			break;
		}
	}
	
	private class SmsThread implements Runnable {
		String number;
		String content;
		public SmsThread(String number, String content) {
			this.number = number;
			this.content = content;
		}
		@Override
		public void run() {
			sendMsm(number, content);
		}
	}

	private void sendMsm(String number, String content) {
		PendingIntent sendPI = PendingIntent.getBroadcast(
				getApplicationContext(), 0, new Intent(SMS_SEND_ACTIOIN), 0);
		PendingIntent deliveredPI = PendingIntent.getBroadcast(
				getApplicationContext(), 0, new Intent(SMS_DELIVERED_ACTION), 0);

		SmsManager smsManager = SmsManager.getDefault();
		ArrayList<String> contents = smsManager.divideMessage(content);
		for (String str : contents) {
			smsManager.sendTextMessage(number, null, str, sendPI, deliveredPI);
		}
	}
}
