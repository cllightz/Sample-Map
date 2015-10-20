package com.example.foobar.samplemap;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Foobar on 2015/10/21.
 */
public class PolylineDecoder
{
	/**
	 * Transform a encoded PolyLine to a Array of GeoPoints.
	 * Java implementation of the original Google JS code.
	 * @see Original encoding part: <a href="http://code.google.com/apis/maps/documentation/polylinealgorithm.html">http://code.google.com/apis/maps/documentation/polylinealgorithm.html</a>
	 * @return Array of all GeoPoints decoded from the PolyLine-String.
	 * @param points String containing the encoded PolyLine.
	 * @throws DecodingException
	 */

	public static ArrayList< LatLng > decodePoints( String points )
	{
		ArrayList< LatLng > res = new ArrayList<>();

		try {
			for ( int index = 0, lat = 0, lng = 0; index < points.length(); ) {
				int shift = 0;
				int result = 0;

				for ( ; ; ) {
					int b = points.charAt( index ) - '?';
					++index;
					result |= ( (b & 31) << shift );
					shift += 5;

					if ( b < 32 ) {
						break;
					}
				}

				lat += ( (result & 1) != 0 ? ~(result >> 1) : result >> 1 );

				shift = 0;
				result = 0;

				for ( ; ; ) {
					int b = points.charAt( index ) - '?';
					++index;
					result |= ( (b & 31) << shift );
					shift += 5;

					if ( b < 32 ) {
						break;
					}
				}

				lng += ( (result & 1) != 0 ? ~(result >> 1) : result >> 1);

				// Add the new Lat/Lng to the Array
				res.add( new LatLng( (double)lat / 100000.0, (double)lng / 100000.0 ) );
			}

			return res;
		} catch ( Exception e ) {
			e.printStackTrace();
		}

		return res;
	}

	public static int[] decodeZoomLevels( String encodedZoomLevels )
	{
		int[] out = new int[ encodedZoomLevels.length() ];
		int index = 0;

		for ( char c : encodedZoomLevels.toCharArray() ) {
			out[index++] = c - '?';
		}

		return out;
	}
}