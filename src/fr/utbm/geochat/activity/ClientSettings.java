package fr.utbm.geochat.activity;

import fr.utbm.geochat.GeoChat;
import fr.utbm.geochat.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ClientSettings extends Activity {
	private RefreshHandler mRedrawHandler = new RefreshHandler(); 
	private View view;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client_settings);
		
		final EditText ipServ = (EditText) findViewById(R.id.ipHostEdit);
		ipServ.setText(""+GeoChat.getInstance().getIpAddress());
		
		final EditText port = (EditText) findViewById(R.id.portHostDestEdit);
		port.setText(""+GeoChat.getInstance().getPort());
		
		final EditText clientUsername = (EditText) findViewById(R.id.clientUsernameEdit);
		clientUsername.setText(""+GeoChat.getInstance().getUsername());

		Button btnBack = (Button) findViewById(R.id.backClientBtn);
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(v.getContext(), ProfileChooser.class));
			}
		});
		
		Button btnConnect = (Button) findViewById(R.id.connectClientBtn);
		btnConnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					GeoChat.getInstance().createClient(mRedrawHandler, clientUsername.getText().toString(), ipServ.getText().toString(), Integer.parseInt(port.getText().toString()));
					GeoChat.appendLog("Host", "Host created");
					view = v;
				} catch (Exception e) {
					GeoChat.appendLog(Log.ERROR, "Host", e.getMessage());
					e.printStackTrace();
		    		Toast.makeText(GeoChat.getInstance().getApplicationContext(), "Error, check connection informations", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	class RefreshHandler extends Handler {  
	    @Override  
	    public void handleMessage(Message msg) { 
	    	switch(msg.what) {
	    	case GeoChat.CONNECTION_ACCEPTED:
		    	startActivity(new Intent(view.getContext(), ChannelList.class));
		    	break;
	    	case GeoChat.CONNECTION_REFUSED:
	    		Toast.makeText(GeoChat.getInstance().getApplicationContext(), (String)msg.obj, Toast.LENGTH_LONG).show();
	    	}
	    }  
	  };  

}
