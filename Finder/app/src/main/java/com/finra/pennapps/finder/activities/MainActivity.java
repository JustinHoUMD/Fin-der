package com.finra.pennapps.finder.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
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
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final String TAG = "MainActivity";
    private ArrayList<ParseObject> al = new ArrayList<ParseObject>();
    private ArrayAdapter<String> arrayAdapter;
    private ImageView finderx, finderheart;
    private GoogleApiClient mGoogleApiClient;
    private LatLng userLocation;
    private SwipeFlingAdapterView flingContainer;
    private Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        buildGoogleApiClient();
        mGoogleApiClient.connect();
        setContentView(R.layout.activity_main);



        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.container);

        /*al = new ArrayList<String>();
        al.add("Justin Ho");
        al.add("Anish Khattar");
        al.add("Taylor Swift");
        al.add("Justin Bieber");*/



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
                // Decrement UserRating
                final String id = ((ParseObject)dataObject).getObjectId();
                //Increment UserRating
                ParseQuery<ParseObject> query = ParseQuery.getQuery("FinraAdvisors");
                // Retrieve the object by id
                query.getInBackground(id, new GetCallback<ParseObject>() {
                    public void done(ParseObject advisor, ParseException e) {
                        if (e == null) {
                            if (advisor.get("UserRating") != null) {
                                advisor.increment("UserRating", -1);
                                advisor.saveInBackground();
                            } else {
                                advisor.put("UserRating", -1);
                                advisor.saveInBackground();
                            }
                        }
                    }
                });
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
                //Toast.makeText(MainActivity.this, "Left!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRightCardExit(Object dataObject) {
                final String id = ((ParseObject)dataObject).getObjectId();
                //Increment UserRating
                ParseQuery<ParseObject> query = ParseQuery.getQuery("FinraAdvisors");
                // Retrieve the object by id
                query.getInBackground(id, new GetCallback<ParseObject>() {
                    public void done(ParseObject advisor, ParseException e) {
                        if (e == null) {
                            if (advisor.get("UserRating") != null) {
                                advisor.increment("UserRating");
                                advisor.saveInBackground();
                            } else {
                                advisor.put("UserRating", 1);
                                advisor.saveInBackground();
                            }
                        }

                    }
                });



                //Toast.makeText(MainActivity.this, "Right!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "dataObject: "+dataObject.toString());
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
                String uid = prefs.getString("uid", "");
                Log.d(TAG, "existing uid: "+uid);

                if (uid.isEmpty()) {
                    Log.d(TAG,"uid is empty");
                    final ParseObject user = new ParseObject("User");

                    ArrayList<String> advisors = new ArrayList<String>();
                    advisors.add(id);
                    user.put("advisors", advisors);
                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            String objectId = user.getObjectId();
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("uid", objectId);
                            editor.apply();
                            Log.d(TAG, "Just set uid to: " + user.getObjectId());
                        }
                    });

                } else {
                    Log.d(TAG,"already have uid");
                    ParseQuery<ParseObject> query2 = ParseQuery.getQuery("User");
                    query2.getInBackground(uid, new GetCallback<ParseObject>() {
                        public void done(ParseObject user, ParseException e) {
                            if (e == null) {
                                user.addAllUnique("advisors", Arrays.asList(id));
                                user.saveInBackground();
                            }
                        }
                    });

                }
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

    public void getAdvisors() {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("FinraAdvisors");
        ParseGeoPoint point = new ParseGeoPoint(userLocation.latitude, userLocation.longitude);
        query.whereNear("advisorLocation", point);
        query.setLimit(200);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                Log.d(TAG, "Just fetched 200 closest advisers");
                al = (ArrayList<ParseObject>) parseObjects;
                Log.d(TAG, "Objects: " + al.toString());
                arrayAdapter = new CardAdapter(getApplicationContext(),  al);
                flingContainer.setAdapter(arrayAdapter);
                arrayAdapter.notifyDataSetChanged();
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
        userLocation = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
        getAdvisors();
        Log.d(TAG,"Lat: "+userLocation.latitude+" Lng: "+userLocation.longitude);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG,"Connection failed");
    }

    public String capitalize(String s){
        StringTokenizer tokenizer = new StringTokenizer(s);
        StringBuffer sb = new StringBuffer();
        while (tokenizer.hasMoreTokens()) {
            String word = tokenizer.nextToken();
            sb.append(word.substring(0, 1).toUpperCase());
            sb.append(word.substring(1).toLowerCase());
            sb.append(' ');
        }
        String text = sb.toString();
        return text;
    }

    private class CardAdapter extends ArrayAdapter {
        private final Context context;
        private ArrayList<ParseObject> advisors;

        public CardAdapter(Context context, List<ParseObject> advisors) {
            super(context, R.layout.card_view, advisors);
            this.context = context;
            this.advisors = (ArrayList<ParseObject>) advisors;
        }

        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View cardView = inflater.inflate(R.layout.card_view, parent, false);
            ParseObject s = advisors.get(position);
            TextView name = (TextView) cardView.findViewById(R.id.name);
            TextView examsPassed = (TextView) cardView.findViewById(R.id.exams_passed);
            TextView yearsExp = (TextView) cardView.findViewById(R.id.yearsExp);
            TextView company = (TextView) cardView.findViewById(R.id.company);
            TextView drpsTextView = (TextView) cardView.findViewById(R.id.drps);
            try {
                // Name
                JSONObject info = s.getJSONObject("Info");
                String firstname = info.getString("@firstNm");
                String lastname = info.getString("@lastNm");
                Log.d(TAG, "Advisor fullname: " + firstname + " " + lastname);
                name.setText(capitalize(firstname + " " + lastname));

                //Exams
                JSONObject exms = s.getJSONObject("Exms");
                int numExams = 0;
                if (exms != null) {
                    JSONArray exams = exms.optJSONArray("Exm");
                    if (exams != null) {
                        numExams = exams.length();
                        Log.d(TAG, "numExams: " + numExams);
                    } else {
                        numExams = 1;
                    }
                }
                examsPassed.setText(String.valueOf(numExams));

                //DRPs
                JSONObject drps = s.getJSONObject("DRPs");
                ArrayList<String> drpList = new ArrayList<String>();
                if (drps != null) {
                    JSONObject drps2 = drps.getJSONObject("DRP");
                    Iterator<String> iter = drps2.keys();
                    while (iter.hasNext()) {
                        String key = iter.next();
                        try {
                            if (drps2.get(key).equals("Y")) {
                                drpList.add(key);
                            }
                        } catch (JSONException e) {
                            // Something went wrong!
                        }
                    }
                }
                String drpsString = "";
                for(String drp:drpList){
                    drpsString = drpsString+" "+drp;
                }
                if (drpsString.isEmpty()) {
                    drpsString = "None";
                }
                drpsString = drpsString.replaceAll("@","");
                drpsTextView.setText("DRPs: "+drpsString);

                //Years Exp
                int exp = 0;
                JSONObject prevReg = s.getJSONObject("PrevRgstns");
                if (prevReg != null) {
                    JSONObject prevRegObj = prevReg.optJSONObject("PrevRgstn");
                    if (prevRegObj != null) {
                        String date = prevRegObj.getString("@regBeginDt");
                        int year = Integer.parseInt(date.substring(0, 4));
                        exp = 2015 - year;
                    } else  {
                        // Find earliest date
                        JSONObject prevReg2 = prevReg.optJSONObject("PrevRgstn");
                        if (prevReg2 != null) {
                            String date = prevReg2.getString("@regBeginDt");
                            int year = Integer.parseInt(date.substring(0, 4));
                            exp = 2015 - year;
                        } else {
                            JSONArray prevRegArr = prevReg.getJSONArray("PrevRgstn");
                            String date = prevRegArr.getJSONObject(0).getString("@regBeginDt");
                            int year = Integer.parseInt(date.substring(0, 4));
                            Log.d(TAG, "year: " + year);
                            exp = 2015 - year;
                        }
                    }
                }else {
                        JSONObject currReg = s.getJSONObject("CrntEmps");
                        if (currReg != null) {
                            Log.d(TAG, "Step 1");
                            JSONObject currReg2 = currReg.getJSONObject("CrntEmp");
                            if (currReg2 != null) {
                                Log.d(TAG, "Step 2");
                                JSONObject currReg3 = currReg2.getJSONObject("CrntRgstns");
                                if (currReg3 != null) {
                                    Log.d(TAG, "Step 3");
                                    JSONObject currReg4 = currReg3.optJSONObject("CrntRgstn");
                                    if (currReg4 != null) {
                                        Log.d(TAG, "Step 5");
                                        String date = currReg4.getString("@stDt");
                                        exp = 2015 - Integer.parseInt(date.substring(0, 4));
                                    } else {
                                        JSONArray currReg5 = currReg3.getJSONArray("CrntRgstn");
                                        String date = currReg5.getJSONObject(0).getString("@stDt");
                                        exp = 2015 - Integer.parseInt(date.substring(0,4));
                                    }
                                }
                            }
                        }

                }
                yearsExp.setText(String.valueOf(exp));

                String companyName = "";
                JSONObject currReg = s.getJSONObject("CrntEmps");
                if (currReg != null) {
                    JSONObject currReg2 = currReg.getJSONObject("CrntEmp");
                    if (currReg2 != null) {
                        companyName = currReg2.getString("@orgNm");
                    }
                }
                company.setText(capitalize(companyName));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return cardView;

        }
    }
}
