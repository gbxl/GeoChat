package fr.utbm.geochat.activity;

import java.util.ArrayList;

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

/**
 * Classe listant les personnes cibles des messages
 * 
 *
 */
public class ReceiverList extends Activity {
	
	private ArrayList<String> receivers = new ArrayList<String>();
    private ReceiverAdapter receiversAdapter;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receiver_list);
        
        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);
        if ( GeoChat.getInstance().getClient() != null) {
			receiversAdapter = new ReceiverAdapter(this, GeoChat.getInstance().getClient().getClients());
		}
		else if( GeoChat.getInstance().getHost() != null) {
			receiversAdapter = new ReceiverAdapter(this, GeoChat.getInstance().getHost().getClients());
		}
        ListView receiverListView = (ListView) findViewById(R.id.receiverList);
        receiverListView.setAdapter(receiversAdapter);
        
        Button okBtn = (Button) findViewById(R.id.receiverOkBtn);
        okBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if (receiversAdapter.getReceivers().size() == 0) {
        			Toast.makeText(v.getContext(), "Please choose at least one receiver", Toast.LENGTH_LONG).show();
				}
            	else {
	            	receivers = new ArrayList<String>(receiversAdapter.getReceivers());
	            	Intent tempI = new Intent();
	            	tempI.putStringArrayListExtra("Receivers", receivers);
	            	setResult(RESULT_OK, tempI);
	                finish();
            	}
            }
        });
        
	}
}
