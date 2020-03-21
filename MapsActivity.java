package ca.mohawk.google_maps;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;


import android.location.Geocoder;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnPolygonClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "Mapactivity" ;
    private GoogleMap mMap;
    private Context context;
    private FusedLocationProviderClient fusedLocationProviderClient;


    private GoogleApiClient client;
    double currentLatitude, currentLongitude;
    Location myLocation;

    private final static int REQUEST_CHECJ_SETTINGS_GPS = 0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS = 0x2;

    int PROXIMITY_RADIUS = 10000;
    double latitude, longitude;

    private EditText mSearch;

    LocationManager locationManager;
    LocationListener locationListener;
    LatLng latLng;

    private boolean locationPermissionGranted;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 404;
    private LocationRequest locationRequest;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;


    private String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    private static final int COLOR_BLACK_ARGB = 0xff000000;


    private static final int POLYGON_STROKE_WIDTH_PX = 2;

    PlacesClient placeClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String apikey = "AIzaSyAHeuiy_ITho0dMl09ZBlNwgujBkpScHOM";

        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(), apikey);
        }

        placeClient = Places.createClient(this);


        final AutocompleteSupportFragment autocompleteSupportFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete);

        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME ));

        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                final LatLng latLng = place.getLatLng();
                mMap.addMarker(new MarkerOptions().position(latLng).title("Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9));
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });




        mSearch = (EditText) findViewById(R.id.search);

        getLocationPermission();

        init();

    }


    private void getDeviceLocation(){

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(locationPermissionGranted){
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Location currentLocation = (Location) task.getResult();

                            latitude = currentLocation.getLatitude();
                            longitude = currentLocation.getLongitude();

                            latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9));
                        }else{
                            Toast.makeText(MapsActivity.this, "unable to get location", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        }catch (SecurityException e){

        }

    }

    private void mapInitialize(){
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void moveCamera(LatLng latlng, float zoom){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    }



    private void getLocationPermission(){
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationPermissionGranted = true;
                mapInitialize();
            }else{
                ActivityCompat.requestPermissions(this,permission,LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,permission,LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        locationPermissionGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length >0 ){
                    for(int i =0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            locationPermissionGranted = false;
                            return;
                        }
                    }
                    locationPermissionGranted = true;
                    //initialize our map
                    mapInitialize();
                }
            }
        }
    }

    private void init()
    {
        Log.d(TAG, "Initialiazing");

        mSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if(actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ){

                    try {
                        geolocate();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
                return false;
            }


        });


    }

    private void geolocate() throws IOException {

        String searchstring = mSearch.getText().toString();
        Geocoder geocoder = new Geocoder(MapsActivity.this);
        List<Address> list = geocoder.getFromLocationName(searchstring, 1);

        if(list.size() > 0){
            Address address = list.get(0);
            Log.d(TAG, "geolocate: found Id" + address.toString());
           // Toast.makeText(this, toString(), Toast.LENGTH_SHORT).show();
            mMap.clear();
            latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9));
        }

    }

    public void onClick(View v){

        Object dataTransfer[] = new Object[2];
        GetNearbyPlacesData getNearbyPlacesData = new GetNearbyPlacesData();


        String restaurant = "restaurant";
        String url = getUrl(latitude, longitude, restaurant);

        dataTransfer[0] = mMap;
        dataTransfer[1] = url;


        getNearbyPlacesData.execute(dataTransfer);
        Toast.makeText(MapsActivity.this, "Showing Nearby Restaurants", Toast.LENGTH_SHORT).show();


    }

    private String getUrl(double latitude, double longitude, String nearbyPlace){
          StringBuilder googlePlaceUrl = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
          googlePlaceUrl.append("location"+latitude+","+longitude);
          googlePlaceUrl.append("&radius="+PROXIMITY_RADIUS);
          googlePlaceUrl.append("&type="+nearbyPlace);
          googlePlaceUrl.append("&sensor=true");
          googlePlaceUrl.append("&key=" + "AIzaSyAHeuiy_ITho0dMl09ZBlNwgujBkpScHOM");

          Log.d("MapsActivity", "url ="+googlePlaceUrl.toString());

          return googlePlaceUrl.toString();


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if(locationPermissionGranted){
            getDeviceLocation();

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.clear(); //clear old locations
                mMap.addMarker(new MarkerOptions().position(latLng).title("Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9));

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        Polygon polygon1 = googleMap.addPolygon(new PolygonOptions()
                .clickable(true)
                .add(
                        new LatLng(43.050460, -79.754198),
                        new LatLng(43.155580, -79.709572),
                        new LatLng(43.143867, -79.658696),
                        new LatLng(43.226308, -79.622159),
                        new LatLng(43.298829, -79.794316),
                        new LatLng(43.301831, -79.791107),
                        new LatLng(43.295900, -79.803936),
                        new LatLng(43.312017, -79.816261),
                        new LatLng(43.307020, -79.828621),
                        new LatLng(43.305521, -79.834114),
                        new LatLng(43.305021, -79.838577),
                        new LatLng(43.301524, -79.845100),
                        new LatLng(43.300274, -79.844070),
                        new LatLng(43.300342, -79.846270),
                        new LatLng(43.285972, -79.868513),
                        new LatLng(43.287299, -79.871315),
                        new LatLng(43.286047, -79.874655),
                        new LatLng(43.287869, -79.877522),
                        new LatLng(43.288142, -79.883644),
                        new LatLng(43.287682, -79.885883),
                        new LatLng(43.289183, -79.886021),
                        new LatLng(43.289770, -79.887359),
                        new LatLng(43.291153, -79.887629),
                        new LatLng(43.308777, -79.912765),
                        new LatLng(43.329781, -79.884473),
                        new LatLng(43.330201, -79.874657),
                        new LatLng(43.342380, -79.861377),
                        new LatLng(43.347419, -79.859644),
                        new LatLng(43.471367, -80.034699),
                        new LatLng(43.464968, -80.042126),
                        new LatLng(43.463260, -80.041547),
                        new LatLng(43.465020, -80.039099),
                        new LatLng(43.464276, -80.037801),
                        new LatLng(43.464276, -80.038209),
                        new LatLng(43.462850, -80.037299),
                        new LatLng(43.458701, -80.031992),
                        new LatLng(43.456923, -80.027501),
                        new LatLng(43.455441, -80.046690),
                        new LatLng(43.456923, -80.049140),
                        new LatLng(43.457515, -80.052814),
                        new LatLng(43.435580, -80.083435),
                        new LatLng(43.418679, -80.078536),
                        new LatLng(43.397725, -80.204334),
                        new LatLng(43.343747, -80.187628),
                        new LatLng(43.333651, -80.248871),
                        new LatLng(43.211175, -80.200693)
                ));

        polygon1.setTag("A");
        stylePolygon(polygon1);


        mMap.setOnPolygonClickListener(this);


    }




    private void stylePolygon(Polygon polygon) {
        String type = "";
        // Get the data object stored with the polygon.
        if (polygon.getTag() != null) {
            type = polygon.getTag().toString();
        }

        List<PatternItem> pattern = null;
        int strokeColor = COLOR_BLACK_ARGB;


        polygon.setStrokePattern(pattern);
        polygon.setStrokeWidth(POLYGON_STROKE_WIDTH_PX);
        polygon.setStrokeColor(strokeColor);



    }

    @Override
    public void onPolygonClick(Polygon polygon) {
        {
            // Flip the values of the red, green, and blue components of the polygon's color.
            int color = polygon.getStrokeColor() ^ 0x00ffffff;
            polygon.setStrokeColor(color);
            color = polygon.getFillColor() ^ 0x00ffffff;
            polygon.setFillColor(color);

            Toast.makeText(this, "Hamilton " + polygon.getTag().toString(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {



    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
