package fr.utbm.geochat.activity;

import fr.utbm.geochat.GeoChat;
import fr.utbm.geochat.R;
import fr.utbm.geochat.client.ClientGeolocalisationService;
import fr.utbm.geochat.host.HostGeolocalisationService;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class OptionMenu extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.option_menu);

		final EditText timeGps = (EditText) findViewById(R.id.timingGeoEdit);
		timeGps.setText("" + GeoChat.getInstance().getTimeGps());

		final CheckBox checkBoxActivation = (CheckBox) findViewById(R.id.activateGeoBox);
		checkBoxActivation.setChecked(GeoChat.getInstance()
				.isGeolocalisationActivate());

		Button cancel = (Button) findViewById(R.id.optionCancelBtn);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivity(new Intent(v.getContext(), ChannelList.class));
			}
		});

		Button validation = (Button) findViewById(R.id.optionOkBtn);
		validation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				GeoChat.getInstance().setGeolocalisationActivate(
						checkBoxActivation.isChecked());
				GeoChat.getInstance().setTimeGps(
						Integer.parseInt(timeGps.getText().toString()));

				if (GeoChat.getInstance().isGeolocalisationActivate()) {
					if (GeoChat.getInstance().getClient() != null) {
						getApplication().startService(
								new Intent(OptionMenu.this,
										ClientGeolocalisationService.class));
					} else if (GeoChat.getInstance().getHost() != null) {
						getApplication().startService(
								new Intent(OptionMenu.this,
										HostGeolocalisationService.class));
					}
				} else if (!GeoChat.getInstance().isGeolocalisationActivate()) {
					if (GeoChat.getInstance().getClient() != null) {
						GeoChat.getInstance().getClient().stopServiceGPS();
					} else if (GeoChat.getInstance().getHost() != null) {
						GeoChat.getInstance().getHost().stopServiceGPS();
					}
				}
				startActivity(new Intent(v.getContext(), ChannelList.class));
			}
		});
	}
}
