package com.example.foobar.samplemap;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
	private GoogleMap mMap;
	private RequestQueue mQueue;
	private LocationManager mManager;
	private boolean mGPSChanged;
	private int mTaps;
	private LatLng mTmpOrigin;
	private Marker mTmpMarker;
	private JsonObjectRequest mTmpRequest;
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_maps );
		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
												.findFragmentById( R.id.map );
		mapFragment.getMapAsync( this );

		mQueue = Volley.newRequestQueue( this );

		// LocationManager
		mManager = (LocationManager)getSystemService( LOCATION_SERVICE );

		// Enable GPS
		if ( !mManager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
			Intent settingsIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS );
			startActivity( settingsIntent );
		}

		mGPSChanged = false;

		mTaps = 0;
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

		// GPSを初めて掴んだ時にカメラを移動
		/*
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
		*/
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

		GoogleMap.OnMapClickListener listener = new GoogleMap.OnMapClickListener() {
			@Override
			public void onMapClick( LatLng tapped ) {
				if ( mTaps == 0 ) {
					mTmpOrigin = tapped;

					MarkerOptions options = new MarkerOptions().position( tapped );
					mTmpMarker = mMap.addMarker( options );

					++mTaps;
				} else if ( mTaps == 1 ) {
					// Directions API
					String key = getResources().getString( R.string.server_key );
					DirectionsAPI dapi = new DirectionsAPI( mMap, key );
					mTmpRequest = dapi.getRequest( mTmpOrigin, tapped, Color.RED );
					mQueue.add( mTmpRequest );

					mTmpMarker.remove();

					mTaps = 0;
				} else {
					Log.e( "OnMapClick", "mTaps != 0 && mTaps != 1" );
				}
			}
		};

		mMap.setOnMapClickListener( listener );

		// Move Camera
		float zoom = 10.0f;
		float tilt = 0.0f;
		float bear = 0.0f;
		LatLng defaultPosition = new LatLng( 35.681061, 139.767096 );
		CameraPosition position = new CameraPosition( defaultPosition, zoom, tilt, bear );
		CameraUpdate update = CameraUpdateFactory.newCameraPosition( position );
		mMap.moveCamera( update );

		/*
		// Directions API
		String key = getResources().getString( R.string.server_key );
		DirectionsAPI dapi = new DirectionsAPI( mMap, key );

		mQueue.add( mDapi.getRequest( new LatLng( 35.613495, 139.744841 ), new LatLng( 35.606229, 139.744285 ), Color.RED ) );
		mQueue.add( mDapi.getRequest( new LatLng( 35.607475, 139.744543 ), new LatLng( 35.608708, 139.743981 ), Color.GREEN ) );
		mQueue.add( mDapi.getRequest( new LatLng( 35.605155, 139.747031 ), new LatLng( 35.604909, 139.743541 ), Color.YELLOW ) );
		mQueue.add( mDapi.getRequest( new LatLng( 35.607519, 139.743220 ), new LatLng( 35.605129, 139.741669 ), Color.BLUE ) );
		*/
	}
}
