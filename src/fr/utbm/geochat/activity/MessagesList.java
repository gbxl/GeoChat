package fr.utbm.geochat.activity;

import fr.utbm.geochat.GeoChat;
import fr.utbm.geochat.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class MessagesList extends Activity {

	private static final int LIST_RECEIVERS = 2;

	private MessagesAdapter adapter;
	private StringBuffer outStringBuffer;
	private ListView conversationView;
	private EditText outEditText;
	private Button sendButton;
	private String receivers;
	private RefreshHandler redrawHandler = new RefreshHandler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messages_list);
		if (GeoChat.getInstance().getClient() != null) {
			adapter = GeoChat.getInstance().getClient().getAdapter()
					.getMessAdapter();
			GeoChat.getInstance().getClient().setRedrawHandler(redrawHandler);
		} else if (GeoChat.getInstance().getHost() != null) {
			adapter = GeoChat.getInstance().getHost().getAdapter()
					.getMessAdapter();
			GeoChat.getInstance().getHost().setRedrawHandler(redrawHandler);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		// Initialize the array adapter for the conversation thread
		conversationView = (ListView) findViewById(R.id.in);
		conversationView.setAdapter(adapter);

		// Initialize the compose field with a listener for the return key
		outEditText = (EditText) findViewById(R.id.edit_text_out);
		outEditText.setOnEditorActionListener(mWriteListener);

		// Initialize the send button with a listener that for click events
		sendButton = (Button) findViewById(R.id.button_send);
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				sendMessage(outEditText.getText().toString());
			}
		});

		// Initialize the buffer for outgoing messages
		outStringBuffer = new StringBuffer("");
	}

	private void sendMessage(String message) {

		// Check that there's actually something to send
		if (message != null) {
			if (GeoChat.getInstance().getHost() != null) {
				GeoChat.getInstance().getHost().sendServer(message);
			} else if (GeoChat.getInstance().getClient() != null) {
				GeoChat.getInstance().getClient().sendServer(message);
			}

			// Reset out string buffer to zero and clear the edit text field
			outStringBuffer.setLength(0);
			outEditText.setText(outStringBuffer);
		}
	}

	// The action listener for the EditText widget, to listen for the return key
	private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView view, int actionId,
				KeyEvent event) {
			// If the action is a key-up event on the return key, send the
			// message
			if (actionId == EditorInfo.IME_NULL
					&& event.getAction() == KeyEvent.ACTION_UP) {
				sendMessage(outEditText.getText().toString());
			}
			return true;
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu_client, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.whisp:
			LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
					.getSystemService(LAYOUT_INFLATER_SERVICE);
			View popupView = layoutInflater.inflate(R.layout.popup_whisp, null);
			final PopupWindow popupWindow = new PopupWindow(popupView,
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			final EditText whispMess = (EditText) popupView
					.findViewById(R.id.popupEditMessWhisp);

			Button cancelButton = (Button) popupView
					.findViewById(R.id.cancelWhispBtn);
			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					popupWindow.dismiss();
				}
			});

			Button chooseDest = (Button) popupView
					.findViewById(R.id.popupWhispChooseDestBtn);
			chooseDest.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent serverIntent = new Intent(v.getContext(),
							ReceiverListUnique.class);
					startActivityForResult(serverIntent, LIST_RECEIVERS);
				}
			});

			Button sendWhisp = (Button) popupView
					.findViewById(R.id.validateWhispBtn);
			sendWhisp.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (GeoChat.getInstance().getClient() != null) {
						GeoChat.getInstance()
								.getClient()
								.whisp(whispMess.getText().toString(),
										receivers);
					} else if (GeoChat.getInstance().getHost() != null) {
						GeoChat.getInstance()
								.getHost()
								.whisp(whispMess.getText().toString(),
										receivers);
					}
					popupWindow.dismiss();
				}
			});

			popupWindow.setFocusable(true);
			popupWindow.showAtLocation(getCurrentFocus(), Gravity.CENTER, 0, 0);

			return true;
		case R.id.send_file:
			startActivity(new Intent(this, FileSender.class));
			return true;
		case R.id.leave:
			if (GeoChat.getInstance().getClient() != null) {
				GeoChat.getInstance().getClient().quitChannel();
			} else if (GeoChat.getInstance().getHost() != null) {
				GeoChat.getInstance().getHost().quitChannel();
			}
			startActivity(new Intent(this, ChannelList.class));
			return true;
		case R.id.map:
			if (GeoChat.getInstance().getClient() != null) {
				GeoChat.getInstance().getClient().requestLocations();
			} else if (GeoChat.getInstance().getHost() != null) {
				// startActivity(new Intent(getApplicationContext(),
				// GeoMap.class));
				GeoChat.getInstance().getHost().launchMap();
			}
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public void onBackPressed() {
		startActivity(new Intent(GeoChat.getInstance().getApplicationContext(),
				ChannelList.class));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case LIST_RECEIVERS:
				if (!data.getStringExtra("Receivers").equals("")) {
					this.receivers = data.getStringExtra("Receivers");
				} else {
					Toast.makeText(this, "Please choose at least one receiver",
							Toast.LENGTH_LONG).show();
				}
				break;
			default:
				break;
			}
		}
	}

	public class RefreshHandler extends Handler {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case GeoChat.VIBRATION_ON:
				Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				long pattern[] = { 0, 1000, 1000, 2000 };
				vib.vibrate(pattern, -1);
				break;
			case GeoChat.LAUNCH_MAP_HANDLE:
				if (GeoChat.getInstance().getClient() != null) {
					if (GeoChat.getInstance().getClient().getLocations().size() != 0) {
						startActivity(new Intent(getApplicationContext(),
								GeoMap.class));
					} else {
						Context context = getApplicationContext();
						CharSequence text = "Map can't be launched";
						int duration = Toast.LENGTH_SHORT;

						Toast toast = Toast.makeText(context, text, duration);
						toast.show();
					}
				} else if (GeoChat.getInstance().getHost() != null) {
					if (GeoChat.getInstance().getHost().getLocations().size() != 0) {
						startActivity(new Intent(getApplicationContext(),
								GeoMap.class));
					} else {
						Context context = getApplicationContext();
						CharSequence text = "Map can't be launched";
						int duration = Toast.LENGTH_SHORT;

						Toast toast = Toast.makeText(context, text, duration);
						toast.show();
					}
				}

				break;
			case GeoChat.FILE_DOWNLOADED_HANDLE:
				Toast.makeText(GeoChat.getInstance().getApplicationContext(),
						(String) msg.obj + " has been downloaded",
						Toast.LENGTH_LONG).show();
				break;
			case GeoChat.FILE_FAIL_HANDLE:
				Toast.makeText(GeoChat.getInstance().getApplicationContext(),
						(String) msg.obj, Toast.LENGTH_LONG).show();
				break;
			case GeoChat.FILE_REQUEST_HANDLE:
				LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
						.getSystemService(LAYOUT_INFLATER_SERVICE);
				View popupView = layoutInflater.inflate(
						R.layout.popup_accept_file, null);
				final PopupWindow popupWindow = new PopupWindow(popupView,
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				TextView fileName = (TextView) popupView
						.findViewById(R.id.popupFileName);
				fileName.setText((String) msg.obj);

				Button refuseButton = (Button) popupView
						.findViewById(R.id.refuseFileBtn);
				refuseButton.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (GeoChat.getInstance().getClient() != null) {
							GeoChat.getInstance().getClient().refuseFile();
						} else if (GeoChat.getInstance().getHost() != null) {
							GeoChat.getInstance().getHost().refuseFile();
						}
						popupWindow.dismiss();
					}
				});

				Button sendWhisp = (Button) popupView
						.findViewById(R.id.acceptFileBtn);
				sendWhisp.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (GeoChat.getInstance().getClient() != null) {
							GeoChat.getInstance().getClient().acceptFile();
						} else if (GeoChat.getInstance().getHost() != null) {
							GeoChat.getInstance().getHost().acceptFile();
						}
						popupWindow.dismiss();
					}
				});

				popupWindow.setFocusable(true);
				popupWindow.showAtLocation(getCurrentFocus(), Gravity.CENTER,
						0, 0);
				break;
			default:
				break;
			}
		}
	}

}
