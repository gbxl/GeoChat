package fr.utbm.geochat.activity;

import fr.utbm.geochat.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ProfileChooser extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button host = (Button) findViewById(R.id.hostServBtn);
		host.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivity(new Intent(v.getContext(), HostSettings.class));
			}
		});

		Button client = (Button) findViewById(R.id.joinServBtn);
		client.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				startActivity(new Intent(v.getContext(), ClientSettings.class));
			}
		});
	}
}
