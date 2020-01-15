package edu.mit.ll.hadr.psiapanalytictester;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspClient;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.mit.ll.hadr.psiapanalytictester.activity.PSIAPSettingsActivity;
import edu.mit.ll.hadr.psiapanalytictester.analytics.model.iva.IVAClassifier;

public class MainActivityBackup extends AppCompatActivity implements RtspClient.Callback, Session.Callback, SurfaceHolder.Callback
{
    private static final String TAG = "PSIAP";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static final int READ_PERMS = 101;
    private static final int WRITE_PERMS = 102;
    private static final int CAMERA_PERMS = 201;

    private BottomNavigationView mNavigation;
    private TextView mTextMessage;
    private Button mStartBtn;

    private Camera mCamera = null;
    private Handler mCameraHandler = null;
    private HandlerThread mCameraThread = null;

    private Executor executor = Executors.newSingleThreadExecutor();
    private IVAClassifier classifier;

    private static final Integer STREAM_STATE = 0;
    private static final Integer CAMERA_STATE = 1;
    private static final Integer FILE_STATE = 2;
    private Integer activeState = 0;

//    surface view
    private SurfaceView mSurfaceView;
    private Session mSession = null;
    private RtspClient client = null;
    private Thread connect_server;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Log.i(TAG, "Navigation item selected: " + item.getItemId());
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    Log.i(TAG, "Stream menu item selected");
                    activeState = STREAM_STATE;
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    Log.i(TAG, "Camera menu item selected");
                    activeState = CAMERA_STATE;
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    Log.i(TAG, "File menu item selected");
                    activeState = FILE_STATE;
                    return true;
                default:
                    Log.i(TAG, "Invalid item id for menu");
            }

            Log.i(TAG, "Returning false");
            return false;
        }
    };

    public void startBtnClickHandler(View v) {
        Log.i(TAG, "Click handler called");

        if (activeState == STREAM_STATE) {
            Log.i(TAG, "STREAM");
            streamCamera();
        } else if (activeState == CAMERA_STATE) {
            Log.i(TAG, "CAMERA");
            predictCamera();
        } else if (activeState == FILE_STATE) {
            Log.i(TAG, "FILE");
            loadFile();
        } else {
            Log.w(TAG, "Invalid active state: " + activeState);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(this);

        mNavigation = (BottomNavigationView) findViewById(R.id.navigation);
        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        initTfModel();

//        // Example of a call to a native method
        mTextMessage = (TextView) findViewById(R.id.message);
//        mStartBtn = (Button) findViewById(R.id.startBtn);


//        mTextMessage.setText(stringFromJNI());
        mSurfaceView = (SurfaceView) findViewById(R.id.cameraPreview_surfaceView);
        mSurfaceView.getHolder().addCallback(this);
        init_rtsp();
    }

    public void init_rtsp()
    {
        if (mSession == null && client == null) {
            mSession = SessionBuilder.getInstance().setContext(getApplicationContext())
                    .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                    .setAudioQuality(new AudioQuality(8000, 16000))
                    .setVideoEncoder(SessionBuilder.VIDEO_H264)
                    .setSurfaceView(mSurfaceView)
                    .setPreviewOrientation(0)
                    .setCallback(this).build();

            // configure client
            client = new RtspClient();
            client.setServerAddress("appserver01.hadr.ll.mit.edu", 123);
            client.setSession(mSession);
            client.setCallback(this);
            mSurfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW);

            // server values
            // get something specific about device
            final String url = "rtsp://appserver01.hadr.ll.mit.edu:123/streams/test123";
//            String ip, port, path;
//            Pattern uri = Pattern.compile("rtsp://(.+):(\\d+)/(.+)");
//            Matcher m = uri.matcher(url);
//            ip = m.group(1);
//            port = m.group(2);
//            path = m.group(3);
            client.setStreamPath("/streams/test123");
//            client.setStreamPath("/" + path);
        }
    }

    @Override
    public void onSessionError(int reason, int streamType, Exception e) {
        Log.e(TAG, "Session error: " + e.getMessage());
        if (e != null) {
            e.printStackTrace();
        }

        switch (reason) {
            case Session.ERROR_CAMERA_ALREADY_IN_USE:

                break;
            case Session.ERROR_CAMERA_HAS_NO_FLASH:

                break;
            case Session.ERROR_CONFIGURATION_NOT_SUPPORTED:

                break;
            case Session.ERROR_INVALID_SURFACE:

                break;
            case Session.ERROR_OTHER:

                break;
            case Session.ERROR_STORAGE_NOT_READY:

                break;
        }


    }

    @Override
    public void onPreviewStarted() {
        Log.i(TAG, "onPreviewStarted!");
    }

    @Override
    public void onSessionConfigured() {
        Log.i(TAG, "onSessionConfigured!");
    }

    @Override
    public void onSessionStarted() {
        Log.i(TAG, "onSessionStarted!");
    }

    @Override
    public void onSessionStopped() {
        Log.i(TAG, "onSessionStopped!");

    }

    @Override
    public void onBitrateUpdate(long bitrate) {
        Log.i(TAG, "onBitrateUpdate!");

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "Surface changed: "+ width +"w x "+ height +"h");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface destroyed");
    }

    @Override
    public void onRtspUpdate(int message, Exception exception)
    {
        Log.d(TAG, "RTSP Update: " + message);

        switch(message)
        {
            case RtspClient.ERROR_CONNECTION_FAILED:
                Log.e(TAG, "RTSP connection failed!");

                break;
            case RtspClient.ERROR_CONNECTION_LOST:
                Log.e(TAG, "RTSP connection lost!");
                // sleep and try to reconnect
                break;
            case RtspClient.ERROR_WRONG_CREDENTIALS:
                Log.e(TAG, "RTSP invalid credentials!");
                break;
        }
    }

    public void start_rtsp()
    {
        if(! client.isStreaming())
        {
            mSession.startPreview();
            client.startStream();
        } else
        {
            mSession.stopPreview();
            client.stopStream();
            mSession.release();
            mSurfaceView.getHolder().removeCallback(this);

        }
    }

    public void initTfModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
//                    String dn = getResources().getResourceName(R.raw.day_night);
//                    String ivad = getResources().getResourceName(R.raw.iva_day);
//                    String ivan = getResources().getResourceName(R.raw.iva_night);
//                    Log.i(TAG, "DN: " + dn);
//                    Log.i(TAG, "IVAD: " + ivad);
//                    Log.i(TAG, "IVAN: " + ivan);
                    classifier = IVAClassifier.create(getAssets());
                    Log.d(TAG, "IVA Initialized");
                } catch (Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow & IVA");
                }
            }
        });
    }

    private void startCamera()
    {
        if (mCameraThread == null) {
            mCameraThread = new HandlerThread("CAMERA_THREAD");
            mCameraThread.start();
            mCameraHandler = new Handler(mCameraThread.getLooper());
        }
    }

    private void streamCamera()
    {
        startCamera();

        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private void predictCamera() {
        startCamera();

        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private void checkPermissions()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            // request permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMS);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        Log.i(TAG, "Request permissions result: " + requestCode);
        switch(requestCode)
        {
            case READ_PERMS:

                break;
            case WRITE_PERMS:

                break;
            case CAMERA_PERMS:

                break;
            default:
                Log.w(TAG, "Invalid request code: " + requestCode);

        }
    }

    public void loadFile()
    {
        checkPermissions();

        // get internal storage path
        Log.i(TAG, "Filedir: " + getFilesDir());
        File[] files = getFilesDir().listFiles();
        for (File f : files) {
            Log.i(TAG, "Found file: "+ f.getAbsolutePath());
        }

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        Log.i(TAG, "Download dir: " + dir.getAbsolutePath());
        for (File f : dir.listFiles())
        {
            Log.i(TAG, "Donwloads file: " + f.getAbsolutePath());
        }

        // use file: VIRB0042.MP4
    }

    /**
     * Inflates the options menu and adds items to the menu.
     *
     * @param menu Options menu
     * @return True if menu is inflated.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handles option menu selections and automatically handles clicks
     * on the Up button in the app bar.
     *
     * @param item Item in options menu
     * @return True if Settings is selected in the options menu.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PSIAPSettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
//    public native String stringFromJNI();

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // clean up threads
        if (executor != null)
        {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        classifier.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Exception closing classifier: " + e.getMessage());
                    }

                    if (mCameraThread != null) {
                        mCameraThread.stop();
                        mCameraThread.destroy();
                    }
                }
            });
        }
    }
}
