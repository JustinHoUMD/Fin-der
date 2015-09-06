package com.finra.pennapps.finder.activities;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by me on 9/5/15.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "P5T1msrll0rI1C7wxkRRv7vFdtQCA9fHgXzw3Pac", "l5HvJ3oMhd4OsJcMVxYtGyWRMGpywVsPcOdVfjkz");
    }
}
