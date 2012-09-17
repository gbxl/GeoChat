package fr.utbm.geochat.activity;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * Classe permettant de parametrer les points sur la carte
 *
 *
 */
public class LisItimizedOverlay extends ItemizedOverlay<OverlayItem> {

	private Context context;
	private ArrayList<OverlayItem> arrayListOverlayItem = new ArrayList<OverlayItem>();

	public LisItimizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	public LisItimizedOverlay(Drawable defaultMarker, Context pContext) {
		super(boundCenterBottom(defaultMarker));
		this.context = pContext;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return arrayListOverlayItem.get(i);
	}

	@Override
	public int size() {
		return arrayListOverlayItem.size();
	}

	@Override
	protected boolean onTap(int index) {
		final OverlayItem item = arrayListOverlayItem.get(index);
		/*AlertDialog.Builder dialog = new AlertDialog.Builder(context);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		*/
		
	 AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(item.getSnippet())
               .setTitle(item.getTitle())
               .setCancelable(true)
               .setPositiveButton("Options", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   Intent intent = new Intent(context, MapTargetOption.class);
                	   intent.putExtra("receiver", item.getSnippet());
                	   context.startActivity(intent);
                       dialog.dismiss();
                   }
               })
               .setNegativeButton("Close window", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       dialog.dismiss();
                   }
               });
        
        AlertDialog alert = builder.create();
        alert.show();
	        
	        
		return true;
	}

	public void addOverlayItem(OverlayItem overlay) {
		arrayListOverlayItem.add(overlay);
		populate();
	}
	
	@Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow)
    {
        if(!shadow)
        {
            super.draw(canvas, mapView, false);
        }
    }
}
