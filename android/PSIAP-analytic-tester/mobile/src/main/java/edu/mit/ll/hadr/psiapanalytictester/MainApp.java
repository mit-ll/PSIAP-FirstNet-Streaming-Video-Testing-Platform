package edu.mit.ll.hadr.psiapanalytictester;

import android.app.Application;

import edu.mit.ll.hadr.psiapanalytictester.utils.PreferencesHelper;
import timber.log.Timber;

public class MainApp extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();
        PreferencesHelper.getInstance().init(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

}
