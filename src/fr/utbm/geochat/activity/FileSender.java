package fr.utbm.geochat.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.utbm.geochat.GeoChat;
import fr.utbm.geochat.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Classe d'interface permettant l'envoie de fichier aux utilisateurs
 *
 *
 */
public class FileSender  extends Activity {
	
private static final int REQUEST_PICK_FILE = 1;
private static final int LIST_RECEIVERS = 2;
	
	private TextView filePathView;
	private Button browseBtn;
	private Button chooseReceiversBtn;
	private Button sendFile;
	private File fileToSend;
	private byte[] fileDataToSend;
	private List<String> receivers;
	private String filename;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_sender);
        
        // Set the views
        filePathView = (TextView)findViewById(R.id.file_path_text_view);
        browseBtn = (Button)findViewById(R.id.start_file_picker_button);
        chooseReceiversBtn = (Button)findViewById(R.id.chooseReceiverBtn);
        sendFile = (Button)findViewById(R.id.sendFileBtn);
        
        browseBtn.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v) {
        		// Create a new Intent for the file picker activity
        		Intent intent = new Intent(v.getContext(), FileBrowser.class);

        		// Set the initial directory to be the sdcard
        		intent.putExtra(FileBrowser.EXTRA_FILE_PATH, Environment.getExternalStorageDirectory().toString());

        		startActivityForResult(intent, REQUEST_PICK_FILE);
        	}
        });
        
        chooseReceiversBtn.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v) {
                Intent serverIntent = new Intent(v.getContext(), ReceiverList.class);
                startActivityForResult(serverIntent, LIST_RECEIVERS);
        	}
        });
        
        sendFile.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v) {
        		if (fileToSend != null && receivers.size() != 0) {
        			if ( GeoChat.getInstance().getClient() != null) {
        				GeoChat.getInstance().getClient().sendFile(fileDataToSend, filename, receivers);
        			}
        			else if( GeoChat.getInstance().getHost() != null) {
        				GeoChat.getInstance().getHost().sendFile(fileDataToSend, filename, receivers);
        			}
        			finish();
        		}
        		else {
        			Toast.makeText(v.getContext(), "Please choose a file !", Toast.LENGTH_LONG).show();
        		}
        	}
        });
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == RESULT_OK) {
			switch(requestCode) {
			case REQUEST_PICK_FILE:
				if(data.hasExtra(FileBrowser.EXTRA_FILE_PATH)) {
					// Get the file path
					fileToSend = new File(data.getStringExtra(FileBrowser.EXTRA_FILE_PATH));
					filename = fileToSend.getName();
					fileDataToSend  = new byte [(int)fileToSend.length()];
					try {
						FileInputStream fis = new FileInputStream(fileToSend);
						BufferedInputStream bis = new BufferedInputStream(fis);
						bis.read(fileDataToSend,0,fileDataToSend.length);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// Set the file path text view
					filePathView.setText(fileToSend.getPath());
				}
				break;
			case LIST_RECEIVERS:
				if (data.getStringArrayListExtra("Receivers").size() != 0) {
					this.receivers = new ArrayList<String>(data.getStringArrayListExtra("Receivers"));
				}
				else {
        			Toast.makeText(this, "Please choose at least one receiver", Toast.LENGTH_LONG).show();
				}
				break;
			}
		}
	}
}
