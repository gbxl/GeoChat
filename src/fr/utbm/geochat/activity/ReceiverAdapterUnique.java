package fr.utbm.geochat.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import fr.utbm.geochat.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;

/**
 * 
 * Classe pour interagir lorsque l'on uniquement un seul utilisateur qui enverra des messages.
 *
 */
public class ReceiverAdapterUnique extends BaseAdapter {
	private Context context;
	private List<String> receivers;
	private String selectedReceivers;
	private CheckedTextView receiverName;
	private boolean isChecked = false;
	
	public ReceiverAdapterUnique(Context context, List<String>users) {
		this.context = context;
		this.receivers = new ArrayList<String>(users);
	}
	
	public String getReceivers() {
		return selectedReceivers;
	}

	public void updateChannels(Set<String> users) {
		this.receivers = new ArrayList<String>(users);
		Collections.sort(this.receivers);
		notifyDataSetChanged();
		
	}

	@Override
	public int getCount() {
		return receivers.size();
	}

	@Override
	public Object getItem(int position) {
		return receivers.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		if (position >= receivers.size()) {
			throw new IndexOutOfBoundsException();
		}
		final String entry = receivers.get(position);
		
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.receiver_name_unique , null);
		}
		
		receiverName = (CheckedTextView) convertView.findViewById(R.id.user_name);
		receiverName.setText(entry);
		receiverName.setOnTouchListener(new View.OnTouchListener() {
		

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (isChecked == false) {
					selectedReceivers = entry;
					isChecked = true;
				}
				else {
					isChecked = false;
					selectedReceivers = "";
				}
				return false;
			}
		});
		return convertView;
	}
}
