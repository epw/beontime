package org.willisson.eric.BeOnTime;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MapController;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.GeoPoint;
import android.graphics.drawable.Drawable;

public class LocationChooser extends MapActivity {
	
	MapView map;
	MyLocationOverlay overlay;
	
	@Override
	public void onCreate (Bundle savedState) {
		super.onCreate (savedState);

		setContentView (R.layout.location);

		map = (MapView) findViewById (R.id.map);
		map.setBuiltInZoomControls (true);

		final MapController mc = map.getController();

		overlay = new MyLocationOverlay (this, map);
		overlay.runOnFirstFix (
			new Runnable () {
				public void run () {
					mc.animateTo (overlay.getMyLocation());
					mc.setZoom (12);
				}
			});

		map.setEnabled (true);
		map.setSatellite (false);
		map.setTraffic (false);
		map.setStreetView (false);
			
		mc.setZoom (14);
	}

	@Override
	protected boolean isRouteDisplayed () {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu (Menu menu) {
		MenuInflater inflater = getMenuInflater ();
		inflater.inflate (R.menu.map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		Intent intent = new Intent ();
		GeoPoint chosen = map.getMapCenter ();
		switch (item.getItemId()) {
		case R.id.choose_location:
			intent.putExtra ("org.willisson.eric.BeOnTime.LAT",
					 chosen.getLatitudeE6());
			intent.putExtra ("org.willisson.eric.BeOnTime.LON",
					 chosen.getLongitudeE6());
			intent.putExtra ("org.willisson.eric.BeOnTime.Event",
					 getIntent().getExtras().getBundle
					 ("org.willisson.eric.BeOnTime.Event"));
			setResult (Activity.RESULT_OK, intent);
			this.finish ();
			return true;
		case R.id.name_location:
			// Do something interesting.
			return true;
		default:
			return super.onOptionsItemSelected (item);
		}
	}
}
