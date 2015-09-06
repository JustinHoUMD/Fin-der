package com.finra.pennapps.finder.activities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.finra.pennapps.finder.R;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class AdvisorActivity extends ActionBarActivity {

    public static final String TAG = "AdvisorActivity";
    private ArrayList<ParseObject> advisors = new ArrayList<ParseObject>();
    private ListView listview;
    private AdvisorAdapter adapter;
    private Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advisor);

        setTitle("Saved Advisors");
        getAdvisors();
        listview = (ListView) findViewById(R.id.advisorList);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG,"Clicked: "+position);
                ParseObject object = advisors.get(position);
                JSONObject info = object.getJSONObject("Info");
                try {
                    String url = info.getString("@link");
                    WebView myWebView = (WebView) findViewById(R.id.webview);
                    myWebView.setVisibility(View.VISIBLE);
                    myWebView.loadUrl(url);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_advisor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void getAdvisors() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String uid = prefs.getString("uid", "");
        if (!uid.isEmpty()) {
            ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
            query.getInBackground(uid, new GetCallback<ParseObject>() {
                public void done(ParseObject object, ParseException e) {
                    if (e == null) {
                        List<String> advisorIDs = object.getList("advisors");
                        for (String id:advisorIDs) {
                            ParseQuery<ParseObject> query2 = ParseQuery.getQuery("FinraAdvisors");
                            query2.getInBackground(id, new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    advisors.add(parseObject);
                                    adapter = new AdvisorAdapter(activity, advisors);
                                    listview.setAdapter(adapter);
                                    adapter.notifyDataSetChanged();
                                    Log.d(TAG, "Advisors: "+advisors.toString());
                                }
                            });
                        }
                    } else {
                        // something went wrong
                    }
                }
            });
        }
    }

    private class AdvisorAdapter extends ArrayAdapter {
        private Context context;
        private ArrayList<ParseObject> advisors;
        public AdvisorAdapter(Context context, List<ParseObject> advisors) {
            super(context, R.layout.advisor_list_item, advisors);
            this.advisors = (ArrayList<ParseObject>) advisors;
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.advisor_list_item, parent, false);
            ParseObject s = (ParseObject) advisors.get(position);
            TextView name = (TextView) rowView.findViewById(R.id.textView);
            TextView companyName2 = (TextView) rowView.findViewById(R.id.textView2);
            JSONObject info = s.getJSONObject("Info");
            try {
                String firstname = info.getString("@firstNm");
                String lastname = info.getString("@lastNm");
                name.setText(capitalize(firstname + " " + lastname));

                String companyName = "";
                JSONObject currReg = s.getJSONObject("CrntEmps");
                if (currReg != null) {
                    JSONObject currReg2 = currReg.getJSONObject("CrntEmp");
                    if (currReg2 != null) {
                        companyName = currReg2.getString("@orgNm");
                    }
                }
                companyName2.setText(capitalize(companyName));

            } catch (JSONException e) {
                e.printStackTrace();
            }


            return rowView;
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


    }
}
