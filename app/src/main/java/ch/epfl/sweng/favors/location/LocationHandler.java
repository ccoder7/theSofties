package ch.epfl.sweng.favors.location;

import android.databinding.ObservableField;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.GeoPoint;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import ch.epfl.sweng.favors.database.User;
import ch.epfl.sweng.favors.main.FavorsMain;
import ch.epfl.sweng.favors.utils.ExecutionMode;

/**
 * Owns all request and management facilities related to location
 */
public class LocationHandler {
    private static final String TAG = "LOCATION_HANDLER";

    private static LocationHandler handler = new LocationHandler(true);
    private static LocationHandler handlerNonRec = new LocationHandler(false);
    public static LocationHandler getHandler(){
        return handler;
    }

    // overload getHandler in order to provide method to get non recurrent handler
    public static LocationHandler getHandler(Boolean recurrent){
        if(recurrent) {
            return handler;
        } else {
            return handlerNonRec;
        }
    }

    private Location lastLocation;
    public ObservableField<String> locationCity = new ObservableField<>();
    public ObservableField<GeoPoint> locationPoint = new ObservableField<>();
    public ObservableField<Location> locationUser = new ObservableField<>();

    protected ch.epfl.sweng.favors.location.Location location = ch.epfl.sweng.favors.location.Location.getInstance();

    private boolean recurrent = false;

    public static float distanceTo(GeoPoint geo) {
        Location favLocation = new Location("favor");
        float distance = Float.MAX_VALUE;

        Location l = LocationHandler.getHandler().locationUser.get();
        if(l != null && geo != null) {
            favLocation.setLatitude(geo.getLatitude());
            favLocation.setLongitude(geo.getLongitude());
            distance = l.distanceTo(favLocation);
            Log.d("Location Debug", "Distance: " + distance + ", FavorLocation " + favLocation.getLatitude()+", "+favLocation.getLongitude() + ", UserLocation " +l.getLatitude()+","+l.getLongitude());
        }
        return distance;
    }

    public static String distanceBetween(GeoPoint geo) {
        float distance = distanceTo(geo);
        String output;
        int switchToMeters = 2500;
        int switchToInt = 100000;
        if (distance == Float.MAX_VALUE) {
            return "There is no Location";
        } else if (distance > switchToInt) {
            output = String.format(Locale.getDefault(), "%.0f", (distance/1000)) + "km";
        } else if (distance > switchToMeters) {
            output = String.format(Locale.getDefault(), "%.1f", (distance/1000)) + "km";
        } else {
            output = String.format(Locale.getDefault(), "%.0f", distance) + "m";
        }
        return output + " away";
    }

    public void isRecurrent(boolean newValue){
        if (!newValue && recurrent) {
            location.removeLocationUpdates();
        }
        if (newValue && !recurrent) {
            location.requestLocationUpdates(locationRequest(), successListerner);
        }
        recurrent = newValue;
    }


    public void permissionFeedback(){
        if(recurrent) {
            location.requestLocationUpdates(locationRequest(), successListerner);
        }
        else updateLocation();
    }


    private LocationRequest locationRequest(){
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(60 * 1000); // 60 seconds
        locationRequest.setFastestInterval(30 * 1000); // 30 seconds
        return locationRequest;
    }


    public LocationHandler(boolean recurrent){
        isRecurrent(recurrent);
    }

    OnSuccessListener<Location> successListerner = new OnSuccessListener<Location>() {
        @Override
        public void onSuccess(Location location) {
            updateLocationInformations(location);
        }
    };

    public void updateLocation() {
        location.getLastLocation(successListerner);
    }


    private boolean updateLocationInformations(Location l){
        if (l == null) {
            Log.e(TAG, "Location object is missing in location update request");
            return false;
        }

        lastLocation = l;
        locationPoint.set(new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude()));
        locationCity.set(getReadableLocation(locationPoint.get()));
        locationUser.set(lastLocation);
        User.getMain().setLocation(locationPoint.get());
        // if desired the user city can automatically be updated every time location changes
        // User.setCity(locationCity.get());
        Log.d("location", "code:1000 - successfully obtained location of user");
        return true;
    }

    private String getReadableLocation(GeoPoint geoPoint){

        if (geoPoint == null) {
            Log.e(TAG, "Location geopoint does not have any content");
            return "Not available";
        }
        //if(!ExecutionMode.getInstance().isTest()) {
            //Geocoder gcd = new Geocoder(FavorsMain.getContext(), Locale.getDefault());
            GeocoderDispatcher gcd = GeocoderDispatcher.getGeocoder(FavorsMain.getContext());
            try {
                List<Address> addresses = gcd.getFromLocation(geoPoint.getLatitude(), geoPoint.getLongitude(), 1);
                if (addresses.size() > 0) return addresses.get(0).getLocality();
            } catch (IOException e) {
                Log.e(TAG, "Failed to get geoPoint information");
            }
            return "Not available";
        /*}
        else{
            return  "Fake Lausanne";
        }*/

    }
}