package ca.mohawk.google_maps;

import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GetNearbyPlacesData extends AsyncTask<Object, String, String > {

    private String googlePlaceData;
    private GoogleMap googleMap;
    String url;

    @Override
    protected String doInBackground(Object... objects) {
        googleMap = (GoogleMap)objects[0];
        url = (String)objects[1];

        DownloadURL downloadURL = new DownloadURL();
        try{
            googlePlaceData = downloadURL.readURL(url);
        } catch (IOException e){
            e.printStackTrace();
        }
        return googlePlaceData;
    }

    @Override
    protected void onPostExecute(String s) {

       List<HashMap<String, String>> nearbyPlaceList;
        DataParser parser = new DataParser();
        nearbyPlaceList = parser.parse(s);
        Log.d("nearbyplacesdata","called parse method");
        showNearbyPlaces(nearbyPlaceList);
    }

    private void showNearbyPlaces(List<HashMap<String, String>> nearbyPlaceList){
        for(int i=0; i<nearbyPlaceList.size(); i++)
        {
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = nearbyPlaceList.get(i);

            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));

            LatLng latLng; //= new LatLng(lat, lng);
            //markerOptions.position(latLng);
            //markerOptions.title(placeName + " : " + vicinity);
            //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

            latLng = new LatLng(lat, lng);



            googleMap.addMarker(new MarkerOptions().position(latLng).title("Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9));
        }
    }
}
