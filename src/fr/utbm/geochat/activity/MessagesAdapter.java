package fr.utbm.geochat.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import fr.utbm.geochat.GeoChat;
import fr.utbm.geochat.Message;
import fr.utbm.geochat.R;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MessagesAdapter extends BaseAdapter {

	private Context context;
	private String idChannel;
	private List<Message> messages;

	public MessagesAdapter(Context context, List<Message> messages,
			String idChannel) {
		this.context = context;
		this.idChannel = idChannel;
		this.messages = new ArrayList<Message>(messages);
		updateMessages(messages);
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void updateMessages(List<Message> messages) {
		this.messages = new ArrayList<Message>(messages);
		notifyDataSetChanged();
		// Collections.sort(this.messages);
	}

	public String getIdChannel() {
		return idChannel;
	}

	@Override
	public int getCount() {
		return messages.size();
	}

	@Override
	public Object getItem(int position) {
		if (position >= messages.size()) {
			throw new IndexOutOfBoundsException();
		}
		return messages.get(position);
	}

	@Override
	public long getItemId(int position) {
		if (position >= messages.size()) {
			throw new IndexOutOfBoundsException();
		}
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {
		if (position >= messages.size()) {
			throw new IndexOutOfBoundsException();
		}

		Message entry = messages.get(position);

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.messages_row, null);
		}

		TextView contact = (TextView) convertView
				.findViewById(R.id.contactName);
		if (GeoChat.getInstance().getClient() != null
				&& entry.getSender()
						.toString()
						.equals(GeoChat.getInstance().getClient().getUsername())) {
			contact.setText("Me : ");
		} else if (GeoChat.getInstance().getHost() != null
				&& entry.getSender().toString()
						.equals(GeoChat.getInstance().getHost().getUsername())) {
			contact.setText("Me : ");
		} else {
			contact.setText(entry.getSender().toString() + " : ");
		}

		TextView content = (TextView) convertView.findViewById(R.id.content);
		if (entry.isWhsip()) {
			content.setTextColor(Color.parseColor("#FF0000"));
		}
		content.setText(entry.getContent());

		TextView date = (TextView) convertView.findViewById(R.id.date);
		Date now = new Date();
		String messageDate = "";
		if (entry.getDate().getDate() == now.getDate()
				&& entry.getDate().getMonth() == now.getMonth()
				&& entry.getDate().getYear() == now.getYear()) {
			messageDate += new SimpleDateFormat("HH:mm")
					.format(entry.getDate());
		} else if (entry.getDate().getDate() == now.getDate() - 1
				&& entry.getDate().getMonth() == now.getMonth()
				&& entry.getDate().getYear() == now.getYear()) {
			messageDate += "Yest";
		} else {
			messageDate += new SimpleDateFormat("dd/MM")
					.format(entry.getDate());
		}
		date.setText(messageDate);

		return convertView;
	}

}