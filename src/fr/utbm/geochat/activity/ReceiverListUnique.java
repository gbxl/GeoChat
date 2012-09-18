package fr.utbm.geochat.activity;

import fr.utbm.geochat.GeoChat;
import fr.utbm.geochat.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ReceiverListUnique extends Activity {

	private String receivers;
	private ReceiverAdapterUnique receiversAdapter;
	private ListView receiverListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.receiver_list);

		// Set result CANCELED incase the user backs out
		setResult(Activity.RESULT_CANCELED);
		if (GeoChat.getInstance().getClient() != null) {
			receiversAdapter = new ReceiverAdapterUnique(this, GeoChat
					.getInstance().getClient().getClients());
		} else if (GeoChat.getInstance().getHost() != null) {
			receiversAdapter = new ReceiverAdapterUnique(this, GeoChat
					.getInstance().getHost().getClients());
		}
		receiverListView = (ListView) findViewById(R.id.receiverList);
		receiverListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		receiverListView.setAdapter(receiversAdapter);

		Button okBtn = (Button) findViewById(R.id.receiverOkBtn);
		okBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (receiversAdapter.getReceivers().equals("")) {
					Toast.makeText(v.getContext(),
							"Please choose at least one receiver",
							Toast.LENGTH_LONG).show();
				} else {
					receivers = receiversAdapter.getReceivers();
					Intent tempI = new Intent();
					tempI.putExtra("Receivers", receivers);
					setResult(RESULT_OK, tempI);
					finish();
				}
			}
		});

	}
}
