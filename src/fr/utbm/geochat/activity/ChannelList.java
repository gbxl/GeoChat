package fr.utbm.geochat.activity;

import java.io.IOException;
import java.util.List;

import fr.utbm.geochat.GeoChat;
import fr.utbm.geochat.R;
import fr.utbm.geochat.host.Channel;
import fr.utbm.geochat.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow;
import android.widget.Toast;

/**
 * Classe de channel permettant d'afficher la liste des channels disponibles dans notre application
 * 
 *
 */
public class ChannelList extends Activity {

	ChannelAdapter adapter;
	private RefreshHandler redrawHandler = new RefreshHandler(); 
	private View tView;
	private int tPosition;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_list);
		
		if (GeoChat.getInstance().getClient() != null) {
			adapter = GeoChat.getInstance().getClient().getAdapter();
			GeoChat.getInstance().getClient().setRedrawHandler(redrawHandler);
		}
		else {
			adapter = GeoChat.getInstance().getHost().getAdapter();
			GeoChat.getInstance().getHost().setRedrawHandler(redrawHandler);
		}
		
		final ListView list = (ListView) findViewById(R.id.channelList);
		list.setAdapter(adapter);
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long index) {
				if ( GeoChat.getInstance().getClient() != null) {
					tView = view;
					tPosition = position;
					GeoChat.getInstance().getClient().requestJoinChannel(((Channel)adapter.getItem(position)).getIdChannel());
					adapter.setMessAdapter(new MessagesAdapter(GeoChat.getInstance().getApplicationContext(), GeoChat.getInstance().getClient().getMessages(((Channel)adapter.getItem(position)).getIdChannel()), ((Channel)adapter.getItem(position)).getIdChannel()));
				}
				else if( GeoChat.getInstance().getHost() != null) {
					tView = view;
					tPosition = position;
					String s = ((Channel)adapter.getItem(position)).getIdChannel();
					GeoChat.getInstance().getHost().requestJoinChannel(s);
					List<Message> l = GeoChat.getInstance().getHost().getMessages(((Channel)adapter.getItem(position)).getIdChannel());
					MessagesAdapter ma = new MessagesAdapter(GeoChat.getInstance().getApplicationContext(), l, s);
					adapter.setMessAdapter(ma);
				}
			}
		});

		Button createChannel = (Button) findViewById(R.id.createChannelBtn);
		createChannel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(v.getContext(), ChannelSettings.class));
			}
		});
	}
	
	public void onRestart() {
		onCreate(null);
	}
	
	public void onResume() {
		onCreate(null);
	}
	
	public class RefreshHandler extends Handler {  
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case GeoChat.VIBRATION_ON:
				Vibrator vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
				long pattern[] = {1000, 1000, 1000, 1000};
				vib.vibrate(pattern, -1);
				break;
			case GeoChat.LOGIN_CHANNEL_FAIL_HANDLE:
				Toast.makeText(tView.getContext(), "Authentification to channel failed, please try again.", Toast.LENGTH_LONG).show();
				break;
			case GeoChat.LOGIN_CHANNEL_SUCCES_HANDLE:
				if (tView == null) {
					startActivity(new Intent(GeoChat.getInstance().getApplicationContext(), MessagesList.class));
				}
				else {
					startActivity(new Intent(tView.getContext(), MessagesList.class));
				}
				break;
			case GeoChat.POPUP_LOGIN_CHANNEL_HANDLE:
				LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
			    View popupView = layoutInflater.inflate(R.layout.popup_login_channel, null);  
			    final PopupWindow popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
			    final EditText channelPassword = (EditText)popupView.findViewById(R.id.popupEditPassword);
			    
			    Button cancelButton = (Button)popupView.findViewById(R.id.cancelPasswordBtn);
			    cancelButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						popupWindow.dismiss();
					}
			    });
			    
			    Button logChannelButton = (Button)popupView.findViewById(R.id.validatePasswordBtn);
			    logChannelButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						if ( GeoChat.getInstance().getClient() != null) {
							GeoChat.getInstance().getClient().requestJoinPrivateChannel(((Channel)adapter.getItem(tPosition)).getIdChannel(), channelPassword.getText().toString());
							adapter.setMessAdapter(new MessagesAdapter(GeoChat.getInstance().getApplicationContext(), GeoChat.getInstance().getClient().getMessages(((Channel)adapter.getItem(tPosition)).getIdChannel()), ((Channel)adapter.getItem(tPosition)).getIdChannel()));
						}
						else if( GeoChat.getInstance().getHost() != null) {
							String s = ((Channel)adapter.getItem(tPosition)).getIdChannel();
							String p = channelPassword.getText().toString();
							GeoChat.getInstance().getHost().requestJoinPrivateChannel(s, p);
							List<Message> l = GeoChat.getInstance().getHost().getMessages(((Channel)adapter.getItem(tPosition)).getIdChannel());
							MessagesAdapter ma = new MessagesAdapter(GeoChat.getInstance().getApplicationContext(), l, s);
							adapter.setMessAdapter(ma);
							popupWindow.dismiss();
						}
					}
			    });
			    popupWindow.setFocusable(true);
			    popupWindow.showAsDropDown(tView, 50, -30);

				break;
			case GeoChat.FILE_DOWNLOADED_HANDLE:
				Toast.makeText(GeoChat.getInstance().getApplicationContext(), (String)msg.obj + " has been downloaded", Toast.LENGTH_LONG).show();
				break;
			case GeoChat.FILE_FAIL_HANDLE:
				Toast.makeText(GeoChat.getInstance().getApplicationContext(), (String)msg.obj, Toast.LENGTH_LONG).show();
				break;
			case GeoChat.FILE_REQUEST_HANDLE:
				LayoutInflater layoutInflaterSd = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
			    View popupViewSd = layoutInflaterSd.inflate(R.layout.popup_accept_file, null);  
			    final PopupWindow popupWindowSd = new PopupWindow(popupViewSd, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 
			    TextView fileName = (TextView)popupViewSd.findViewById(R.id.popupFileName);
			    fileName.setText((String)msg.obj);
			    
			    Button refuseButton = (Button)popupViewSd.findViewById(R.id.refuseFileBtn);
			    refuseButton.setOnClickListener(new OnClickListener(){
			    	@Override
			    	public void onClick(View arg0) {
			    		if ( GeoChat.getInstance().getClient() != null) {
			    			GeoChat.getInstance().getClient().refuseFile();
			    		}
			    		else if( GeoChat.getInstance().getHost() != null) {
			    			GeoChat.getInstance().getHost().refuseFile();
			    		}
			    		popupWindowSd.dismiss();
			    	}
			    });
			    
			    Button acceptFile = (Button)popupViewSd.findViewById(R.id.acceptFileBtn);
			    acceptFile.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						if ( GeoChat.getInstance().getClient() != null) {
							GeoChat.getInstance().getClient().acceptFile();
						}
						else if( GeoChat.getInstance().getHost() != null) {
							GeoChat.getInstance().getHost().acceptFile();
						}
						popupWindowSd.dismiss();
					}
			    });

			    popupWindowSd.setFocusable(true);
			    popupWindowSd.showAtLocation(getCurrentFocus(),Gravity.CENTER, 0, 0);
				break;
			}
		}  
	}; 
	
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        MenuInflater inflater = getMenuInflater();
	        inflater.inflate(R.menu.option_menu_channel_list, menu);
	        return true;
	    }

	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	        case R.id.optionsMenuBtn:
	        	startActivity(new Intent(this, OptionMenu.class));
	        	break;
	        case R.id.quitOptionMenuBtn:
	        	if ( GeoChat.getInstance().getClient() != null) {
					GeoChat.getInstance().getClient().exit();
				}
				else if( GeoChat.getInstance().getHost() != null) {
					try {
						GeoChat.getInstance().getHost().exit();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
	        	break;
	        }
	        return false;
	    }

}
