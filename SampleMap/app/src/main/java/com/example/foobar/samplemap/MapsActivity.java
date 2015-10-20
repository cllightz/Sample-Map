package com.example.foobar.samplemap;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
	private GoogleMap mMap;
	private LocationManager mManager;
	private boolean mGPSChanged;
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_maps );
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
												.findFragmentById( R.id.map );
		mapFragment.getMapAsync( this );

		// LocationManager
		mManager = (LocationManager)getSystemService( LOCATION_SERVICE );

		// Enable GPS
		if ( !mManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
			Intent settingsIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
			startActivity( settingsIntent );
		}

		mGPSChanged = false;
	}

	@Override
	protected void onResume() {
		if ( mManager != null ) {
			mManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 1000, 50, this );
		}

		super.onResume();
	}

	@Override
	protected void onPause()
	{
		if ( mManager != null ) {
			mManager.removeUpdates( this );
		}

		super.onPause();
	}

	@Override
	public void onLocationChanged( Location location )
	{
		double lat = location.getLatitude();
		double lng = location.getLongitude();
		LatLng current = new LatLng( lat, lng );
		Log.e( "GPS", "changed: " + lat + ", " + lng );

		if ( ! mGPSChanged ) {
			// Move Camera
			float zoom = 17.0f;
			float tilt = 0.0f;
			float bear = 0.0f;
			CameraPosition position = new CameraPosition( current, zoom, tilt, bear );
			CameraUpdate update = CameraUpdateFactory.newCameraPosition( position );
			mMap.moveCamera( update );

			mGPSChanged = true;
		}
	}

	@Override
	public void onStatusChanged( String provider, int status, Bundle extras )
	{
		switch (status) {
			case LocationProvider.AVAILABLE:
				Log.e( "GPS Status Changed", "AVAILABLE" );
				break;

			case LocationProvider.OUT_OF_SERVICE:
				Log.e( "GPS Status Changed", "OUT_OF_SERVICE" );
				break;

			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				Log.e( "GPS Status Changed", "TEMPORARILY_UNAVAILABLE" );
				break;
		}
	}

	@Override
	public void onProviderEnabled( String provider ) { }

	@Override
	public void onProviderDisabled( String provider ) { }

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
	public void onMapReady( GoogleMap googleMap )
	{
		mMap = googleMap;
		RequestQueue mQueue;

		/* Marker
		// Add a marker in Sydney and move the camera
		LatLng sydney = new LatLng(-34, 151);
		mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
		mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
		*/

		/*
		// Add Polylines
		LatLng sta = new LatLng( 35.606418, 139.749556 );
		LatLng end = new LatLng( 35.604742, 139.748413 );
		PolylineOptions options = new PolylineOptions().add( sta, end ).color( Color.RED );
		mMap.addPolyline( options );
		*/

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

		Log.e( "uri", builder.build().toString() );
		// Toast.makeText( MapsActivity.this, builder.build().toString(), Toast.LENGTH_SHORT ).show();

		// Volley
		mQueue = Volley.newRequestQueue( this );

		Response.Listener< JSONObject > listener = new Response.Listener< JSONObject >() {
			@Override
			public void onResponse( JSONObject response ) throws JSONException {
				Log.e( "volley", "success" );

				JSONArray steps = response.getJSONArray( "routes" )
														.getJSONObject( 0 )
														.getJSONArray( "legs" )
														.getJSONObject( 0 )
														.getJSONArray( "steps" );

				for ( int i = 0; i < steps.length(); ++i ) {
					JSONObject step = steps.getJSONObject( i );

					JSONObject staLoc = step.getJSONObject( "start_location" );
					LatLng sta = new LatLng( staLoc.getDouble( "lat" ), staLoc.getDouble( "lng" ) );

					JSONObject endLoc = step.getJSONObject( "end_location" );
					LatLng end = new LatLng( endLoc.getDouble( "lat" ), endLoc.getDouble( "lng" ) );

					// PolylineOptions options = new PolylineOptions().add( sta, end ).color( Color.RED );

					String points = step.getJSONObject( "polyline" ).getString( "points" );
					ArrayList< LatLng > pointList = PolylineDecoder.decodePoints( points );
					Log.e( "decoder", points );
					PolylineOptions options = new PolylineOptions().color( Color.RED );

					for ( LatLng point : pointList ) {
						Log.e( "lat", String.valueOf( point.latitude ) );
						Log.e( "lng", String.valueOf( point.longitude ) );
						options.add( point );
					}

					mMap.addPolyline( options );
				}
			}
		};

		Response.ErrorListener error = new Response.ErrorListener() {
			@Override
			public void onErrorResponse( VolleyError error ) {
				Log.e( "volley", "error" );
			}
		};

		JsonObjectRequest request = new JsonObjectRequest( Request.Method.GET, builder.build().toString(), null, listener, error );
		mQueue.add( request );
	}
}
