package fr.utbm.geochat.activity;

import fr.utbm.geochat.GeoChat;
import fr.utbm.geochat.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

/**
 * 
 * Classe d'affichage offrant des options d'interactions avec les autres utilisateur
 * à partir de la carte.
 *
 */
public class MapTargetOption extends Activity {
	private String receiver;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_popup_options);
		
		Intent intent = getIntent();
		receiver = intent.getStringExtra("receiver");

		Button whisp = (Button)findViewById(R.id.whispTargetBtn);
		whisp.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				LayoutInflater layoutInflater = (LayoutInflater)getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);  
				View popupView = layoutInflater.inflate(R.layout.popup_whisp_unique, null);  
				final PopupWindow popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
				final EditText whispMess = (EditText)popupView.findViewById(R.id.popupEditMessWhisp);

				Button cancelButton = (Button)popupView.findViewById(R.id.cancelWhispBtn);
				cancelButton.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						popupWindow.dismiss();
					}
				});
				
				Button sendWhisp = (Button)popupView.findViewById(R.id.validateWhispBtn);
				sendWhisp.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						if ( GeoChat.getInstance().getClient() != null) {
							GeoChat.getInstance().getClient().whisp(whispMess.getText().toString(), receiver);
						}
						else if( GeoChat.getInstance().getHost() != null) {
							GeoChat.getInstance().getHost().whisp(whispMess.getText().toString(), receiver);
						}
						popupWindow.dismiss();
					}
				});

				popupWindow.setFocusable(true);
				popupWindow.showAtLocation(v,Gravity.CENTER, 0, 0);

			}
		});
		
		Button wizz = (Button)findViewById(R.id.wizzTargetBtn);
		wizz.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if ( GeoChat.getInstance().getClient() != null) {
					GeoChat.getInstance().getClient().wizz(receiver);
				}
				else if( GeoChat.getInstance().getHost() != null) {
					GeoChat.getInstance().getHost().wizz(receiver);
				}
				Toast.makeText(v.getContext(), receiver + " has been wizzed !", Toast.LENGTH_LONG).show();
			}
		});
		
		Button back = (Button)findViewById(R.id.mapPopupBackBtn);
		back.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
}
