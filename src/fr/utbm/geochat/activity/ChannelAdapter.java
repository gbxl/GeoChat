package fr.utbm.geochat.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import fr.utbm.geochat.GeoChat;
import fr.utbm.geochat.Message;
import fr.utbm.geochat.R;
import fr.utbm.geochat.host.Channel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 
 * Classe de channelAdapter permettant la communication entre l'interface et le systeme
 *
 */
public class ChannelAdapter extends BaseAdapter {
	
	private List<Channel> channels;
	private MessagesAdapter messageAdapter = null;
	private Context context;
	
	public ChannelAdapter(Context context, Set<Channel> channels) {
		this.context = context;
		this.channels = new ArrayList<Channel>(channels);
		List <Message> messages = new LinkedList<Message>();
		messages.add(new Message("#SYSTEM#", new Date(), "Welcome"));
		this.messageAdapter = new MessagesAdapter(context, messages, "Main");
		updateChannels(channels);
	}
	
	public List<Channel> getChannels() {
		return channels;
	}

	public void updateChannels(Set<Channel> channels) {
		Collections.sort(this.channels);
		this.channels = new ArrayList<Channel>(channels);
		if (messageAdapter != null && this.channels.contains(messageAdapter.getIdChannel())) {
			if (GeoChat.getInstance().getClient() != null) {
				messageAdapter.updateMessages(GeoChat.getInstance().getClient().getMessages(messageAdapter.getIdChannel()));
			}
			else {
				messageAdapter.updateMessages(GeoChat.getInstance().getHost().getMessages(messageAdapter.getIdChannel()));
			}
			messageAdapter.notifyDataSetChanged();
		}
		this.notifyDataSetChanged();
	}
	
	public MessagesAdapter getMessAdapter() {
		return messageAdapter;
	}

	public void setMessAdapter(MessagesAdapter messAdapter) {
		this.messageAdapter = messAdapter;
	}

	@Override
	public int getCount() {
		return channels.size();
	}

	@Override
	public Object getItem(int position) {
		if (position >= channels.size()) {
			throw new IndexOutOfBoundsException();
		}
		return channels.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewGroup) {

		if (position >= channels.size()) {
			throw new IndexOutOfBoundsException();
		}
		Channel entry = channels.get(position);
		
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.channel_row, null);
		}
		
		TextView channelRow = (TextView) convertView.findViewById(R.id.channelRow);
		channelRow.setText(entry.getIdChannel());
		
		TextView channelNbClients = (TextView) convertView.findViewById(R.id.channelNbClients);
		channelNbClients.setText(""+entry.getUsers().size());
		
		return convertView;
	}
}
