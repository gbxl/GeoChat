package fr.utbm.geochat.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.utbm.geochat.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

public class ReceiverAdapter extends BaseAdapter {
	private Context context;
	private List<String> receivers;
	private Set<String> selectedReceivers = new HashSet<String>();

	public ReceiverAdapter(Context context, List<String> users) {
		this.context = context;
		this.receivers = new ArrayList<String>(users);
	}

	public Set<String> getReceivers() {
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
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.receiver_name, null);
		}

		CheckBox receiverName = (CheckBox) convertView
				.findViewById(R.id.receiverNameView);
		receiverName.setText(entry);
		receiverName.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (v.isSelected() == false) {
					v.setSelected(true);
					selectedReceivers.add(entry);
				} else {
					v.setSelected(false);
					selectedReceivers.remove(entry);
				}
			}
		});

		return convertView;
	}

}
