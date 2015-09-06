package com.finra.pennapps.finder.activities;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.finra.pennapps.finder.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.lorentzos.flingswipe.SwipeFlingAdapterView;
import com.parse.Parse;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final String TAG = "MainActivity";
    private ArrayList<String> al;
    private ArrayAdapter<String> arrayAdapter;
    private ImageView finderx, finderheart;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "P5T1msrll0rI1C7wxkRRv7vFdtQCA9fHgXzw3Pac", "l5HvJ3oMhd4OsJcMVxYtGyWRMGpywVsPcOdVfjkz");
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        setContentView(R.layout.activity_main);



        final SwipeFlingAdapterView flingContainer = (SwipeFlingAdapterView) findViewById(R.id.container);

        al = new ArrayList<String>();
        al.add("Justin Ho");
        al.add("Anish Khattar");
        al.add("Taylor Swift");
        al.add("Justin Bieber");

        //choose your favorite adapter
        arrayAdapter = new CardAdapter(this,  al );

        finderx = (ImageView) findViewById(R.id.finderx);
        finderheart = (ImageView) findViewById(R.id.finderheart);
        finderx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flingContainer.getTopCardListener().selectLeft();
            }
        });
        finderheart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Store financial advisor
                flingContainer.getTopCardListener().selectRight();
            }
        });

        //set the listener and the adapter
        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {
                // this is the simplest way to delete an object from the Adapter (/AdapterView)
                Log.d("LIST", "removed object!");
                al.remove(0);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                Toast.makeText(MainActivity.this, "Left!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                Toast.makeText(MainActivity.this, "Right!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here

            }

            @Override
            public void onScroll(float v) {

            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_listadvisors) {
            //Go to ListActivity
            Intent intent = new Intent(this, AdvisorActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG,"Connected");
        //Get user's last known location
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        LatLng lastLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
        Log.d(TAG,"Lat: "+lastLocation.latitude+" Lng: "+lastLocation.longitude);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG,"Connection failed");
    }

    private class CardAdapter extends ArrayAdapter {
        private final Context context;
        private ArrayList<String> advisors;

        public CardAdapter(Context context, List<String> advisors) {
            super(context, R.layout.card_view, advisors);
            this.context = context;
            this.advisors = (ArrayList<String>) advisors;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View cardView = inflater.inflate(R.layout.card_view, parent, false);
            String s = advisors.get(position);
            TextView name = (TextView) cardView.findViewById(R.id.name);
            name.setText(s);
            return cardView;

        }
    }
}
