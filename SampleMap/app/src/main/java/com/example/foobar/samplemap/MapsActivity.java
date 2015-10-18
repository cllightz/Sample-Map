package com.example.foobar.samplemap;

import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
	
	private GoogleMap mMap;
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_maps );
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
												.findFragmentById( R.id.map );
		mapFragment.getMapAsync( this );
	}
	
	
	/**
	 * Manipulates the map once available.
	 * This callback is triggered when the map is ready to be used.
	 * This is where we can add markers or lines, add listeners or move the camera. In this case,
	 * we just add a marker near Sydney, Australia.
	 * If Google Play services is not installed on the device, the user will be prompted to install
	 * it inside the SupportMapFragment. This method will only be triggered once the user has
	 * installed Google Play services and returned to the app.
	 */
	@Override
	public void onMapReady( GoogleMap googleMap ) {
		mMap = googleMap;

		/* Marker
		// Add a marker in Sydney and move the camera
		LatLng sydney = new LatLng(-34, 151);
		mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
		mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
		*/

		// Add Polylines
		LatLng sta = new LatLng( 35.606418, 139.749556 );
		LatLng end = new LatLng( 35.604742, 139.748413 );
		PolylineOptions options = new PolylineOptions().add( sta, end ).color( Color.RED );
		mMap.addPolyline( options );

		// Move Camera
		float zoom = 17.0f;
		float tilt = 0.0f;
		float bear = 0.0f;
		CameraPosition position = new CameraPosition( sta, zoom, tilt, bear );
		CameraUpdate update = CameraUpdateFactory.newCameraPosition( position );
		mMap.moveCamera( update );

		// Get a Direction
		String key = getResources().getString( R.string.server_key );
		Uri.Builder builder = new Uri.Builder();
		builder.scheme( "https" );
		builder.authority( "maps.googleapis.com" );
		builder.path( "/maps/api/directions/json" );
		builder.appendQueryParameter( "origin", "35.608787,139.749507" );
		builder.appendQueryParameter( "destination", "35.606125,139.749354" );
		builder.appendQueryParameter( "mode", "walking" );
		builder.appendQueryParameter( "avoid", "indoor" );
		builder.appendQueryParameter( "units", "metric" );
		builder.appendQueryParameter( "key", key );

		Log.d( "uri", builder.build().toString() );
		// Toast.makeText( MapsActivity.this, builder.build().toString(), Toast.LENGTH_SHORT ).show();
	}
}
