package fr.utbm.geochat.activity;

import fr.utbm.geochat.GeoChat;
import fr.utbm.geochat.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ChannelSettings extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_settings);

		final EditText channelName = (EditText) findViewById(R.id.channelNameEdit);
		final EditText channelPassword = (EditText) findViewById(R.id.channelPasswordEdit);

		Button btnBack = (Button) findViewById(R.id.channelCancelBtn);
		btnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(v.getContext(), ChannelList.class));
			}
		});

		Button btnConnect = (Button) findViewById(R.id.channelOKBtn);
		btnConnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String chanName = channelName.getText().toString();
				String chanPassword = channelPassword.getText().toString();

				if (GeoChat.getInstance().getHost() != null) {
					if (!chanName.equals("") && chanPassword.equals("")) {
						GeoChat.getInstance().getHost().createChannel(chanName);
					} else if (!chanName.equals("") && !chanPassword.equals("")) {
						GeoChat.getInstance().getHost()
								.createPrivateChannel(chanName, chanPassword);
					}
				} else if (GeoChat.getInstance().getClient() != null) {
					if (!chanName.equals("") && chanPassword.equals("")) {
						GeoChat.getInstance().getClient()
								.createChannel(chanName);
					} else if (!chanName.equals("") && !chanPassword.equals("")) {
						GeoChat.getInstance().getClient()
								.createPrivateChannel(chanName, chanPassword);
					}
				}

				startActivity(new Intent(v.getContext(), ChannelList.class));
			}
		});

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
