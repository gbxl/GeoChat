package fr.utbm.geochat.activity;

import fr.utbm.geochat.GeoChat;
import fr.utbm.geochat.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 
 * Classe permettant d'afficher les parametres de créations d'un serveur
 *
 */
public class HostSettings extends Activity {
	
	WifiLock wifiLock;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.host_settings);
		checkWifiState();
		
		final EditText port = (EditText) findViewById(R.id.portEdit);
		port.setText(""+GeoChat.getInstance().getPort());
		
		final EditText nbMaxCli = (EditText) findViewById(R.id.maxClientsEdit);
		nbMaxCli.setText(""+GeoChat.getInstance().getNbMaxClients());
		
		final EditText nbChannelPerCli = (EditText) findViewById(R.id.maxChannelsEdit);
		nbChannelPerCli.setText(""+GeoChat.getInstance().getNbMaxChannelPerClient());
		
		final EditText sizeMaxFile = (EditText) findViewById(R.id.maxFileSizeEdit);
		sizeMaxFile.setText(""+GeoChat.getInstance().getMaxSizeFile());
		
		final EditText hostUsername = (EditText) findViewById(R.id.hostNameEdit);
		hostUsername.setText(""+GeoChat.getInstance().getUsername());

		Button btnBack = (Button) findViewById(R.id.cancelBtn);
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(v.getContext(), ProfileChooser.class));
			}
		});
		
		Button btnConnect = (Button) findViewById(R.id.launchHostBtn);
		btnConnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					GeoChat.getInstance().createHost(hostUsername.getText().toString(), Integer.parseInt(port.getText().toString()), Integer.parseInt(nbMaxCli.getText().toString()), Integer.parseInt(nbChannelPerCli.getText().toString()), Integer.parseInt(sizeMaxFile.getText().toString()));
					GeoChat.appendLog("Host", "Host created");
				} catch (Exception e) {
					GeoChat.appendLog(Log.ERROR, "Host", e.getMessage());
					e.printStackTrace();
				}
				startActivity(new Intent(v.getContext(), ChannelList.class));
			}
		});
	}
	
    @Override
    public void onDestroy() {
        super.onDestroy();
        wifiLock.release();
    }
	
	private void checkWifiState() {
		WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);  
		if(!wifiManager.isWifiEnabled()){  
			//wifiManager.setWifiEnabled(true);
			Toast.makeText(getApplicationContext(), "Enable your wifi conection please.", Toast.LENGTH_LONG).show();
			GeoChat.appendLog("Wifi", "Wifi enabled");
			
			Intent discoverableIntent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
			startActivity(discoverableIntent);
			
		}
		wifiLock = wifiManager.createWifiLock("wifilock");
		wifiLock.acquire();
	}

}
