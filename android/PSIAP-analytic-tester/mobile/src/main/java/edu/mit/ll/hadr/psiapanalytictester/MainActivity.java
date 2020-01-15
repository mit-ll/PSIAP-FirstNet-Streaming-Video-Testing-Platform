package edu.mit.ll.hadr.psiapanalytictester;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
//import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.rtsp.RtspClient;

import android.opengl.GLSurfaceView;

import java.io.File;
import com.github.devicehive.client.model.DHResponse;
import com.github.devicehive.client.model.DeviceNotification;
import com.github.devicehive.client.model.FailureData;
import com.github.devicehive.client.service.Device;
import com.github.devicehive.client.service.DeviceCommand;
import com.github.devicehive.client.service.DeviceHive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import edu.mit.ll.hadr.psiapanalytictester.activity.Example2;
import edu.mit.ll.hadr.psiapanalytictester.activity.PSIAPSettingsActivity;
import edu.mit.ll.hadr.psiapanalytictester.analytics.AnalyticUtils;
import edu.mit.ll.hadr.psiapanalytictester.analytics.model.iva.IVAClassifier;
import edu.mit.ll.hadr.psiapanalytictester.task.DeviceHiveAsyncTask;
import edu.mit.ll.hadr.psiapanalytictester.utils.PSIAPUtils;
import edu.mit.ll.hadr.psiapanalytictester.utils.PreferencesHelper;
import edu.mit.ll.hadr.psiapanalytictester.utils.TextWatcherCreator;
import edu.mit.ll.hadr.psiapanalytictester.ws.DeviceHiveWebsocketClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;


public class MainActivity extends AppCompatActivity implements RtspClient.Callback, Session.Callback, SurfaceHolder.Callback
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
    private DeviceHiveWebsocketClient dhWsClient = null;

    private static final Integer STREAM_STATE = 0;
    private static final Integer CAMERA_STATE = 1;
    private static final Integer FILE_STATE = 2;
    private Integer activeState = 0;

//    surface view
//    private GLSurfaceView mSurfaceView;
    private Session mSession = null;
    private RtspClient client = null;
    private Thread connect_server;
    @BindView(R.id.serverAddress)
    TextInputEditText serverAddress;

    @BindView(R.id.deviceId)
    TextInputEditText deviceId;

    @BindView(R.id.accessToken)
    TextInputEditText accessToken;

    @BindView(R.id.refreshToken)
    TextInputEditText refreshToken;

    @BindView(R.id.rtspUrl)
    TextInputEditText rtspUrl;

    @BindView(R.id.deviceIdLayout)
    TextInputLayout deviceIdLayout;

    @BindView(R.id.refreshTokenLayout)
    TextInputLayout refreshTokenLayout;

    @BindView(R.id.serverAddressLayout)
    TextInputLayout serverAddressLayout;

    @BindView(R.id.accessTokenLayout)
    TextInputLayout accessTokenLayout;

    @BindView(R.id.rtspUrlLayout)
    TextInputLayout rtspUrlLayout;

    @BindView(R.id.container)
    View container;

    @BindView(R.id.start)
    Button startButton;

    private DeviceHive deviceHive = null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
//                    mTextMessage.setText(R.string.title_home);
                    Log.d(TAG, "Stream menu item selected");
                    activeState = STREAM_STATE;
                    return true;
                case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);
                    Log.d(TAG, "Camera menu item selected");
                    activeState = CAMERA_STATE;
                    return true;
                case R.id.navigation_notifications:
//                    mTextMessage.setText(R.string.title_notifications);
                    Log.d(TAG, "File menu item selected");
                    activeState = FILE_STATE;
                    return true;
                default:
                    Log.w(TAG, "Invalid item id for menu");
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
            loadFiles();
        } else {
            Log.w(TAG, "Invalid active state: " + activeState);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mNavigation = (BottomNavigationView) findViewById(R.id.navigation);
        mNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // tensorflow
        initTfModel();

//        mTextMessage = (TextView) findViewById(R.id.message);
        mStartBtn = (Button) findViewById(R.id.start);

//        mTextMessage.setText(stringFromJNI());
//        mSurfaceView = (GLSurfaceView) findViewById(R.id.mSurfaceView);
//        mSurfaceView.getHolder().addCallback(this);
        init_rtsp();
    }

    public void init_rtsp()
    {
//        if (mSession == null && client == null) {
//            mSession = SessionBuilder.getInstance().setContext(getApplicationContext())
//                    .setAudioEncoder(SessionBuilder.AUDIO_AAC)
//                    .setAudioQuality(new AudioQuality(8000, 16000))
//                    .setVideoEncoder(SessionBuilder.VIDEO_H264)
//                    .setSurfaceView(mSurfaceView)
//                    .setPreviewOrientation(0)
//                    .setCallback(this).build();
//
//            // configure client
//            client = new RtspClient();
//            client.setServerAddress("appserver01.hadr.ll.mit.edu", 123);
//            client.setSession(mSession);
//            client.setCallback(this);
//            mSurfaceView.setAspectRatioMode(SurfaceView.ASPECT_RATIO_PREVIEW);
//
//            // server values
//            // get something specific about device
//            final String url = "rtsp://appserver01.hadr.ll.mit.edu:123/streams/test123";
////            String ip, port, path;
////            Pattern uri = Pattern.compile("rtsp://(.+):(\\d+)/(.+)");
////            Matcher m = uri.matcher(url);
////            ip = m.group(1);
////            port = m.group(2);
////            path = m.group(3);
//            client.setStreamPath("/streams/test123");
////            client.setStreamPath("/" + path);
//        }
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
//            mSurfaceView.getHolder().removeCallback(this);

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

    private void sendDhTest()
    {
        if (!serverAddress.getText().toString().isEmpty() &&
                !deviceId.getText().toString().isEmpty() &&
                !accessToken.getText().toString().isEmpty() &&
                !refreshToken.getText().toString().isEmpty() &&
                !rtspUrl.getText().toString().isEmpty() )
        {
            final String server = serverAddress.getText().toString().trim();
            final String dId = deviceId.getText().toString().trim();
            final String aToken = accessToken.getText().toString().trim();
            final String rToken = refreshToken.getText().toString().trim();
            final String rtsp = rtspUrl.getText().toString().toString().trim();

            if (deviceHive == null) {
                deviceHive = AnalyticUtils.createDeviceHiveIfNotExists(server, rToken, aToken);
            }

            try {
                Log.i(TAG, "Sending DH test!");
                DeviceHiveAsyncTask task = new DeviceHiveAsyncTask();
                task.setDeviceHive(deviceHive);
                task.execute(deviceId.getText().toString());

//                deviceHive.enableDebug(true);
//                DHResponse<Device> deviceResponse = deviceHive.getDevice(dId);
//                Log.i(TAG, "Got device response");
//                if (deviceResponse.isSuccessful()) {
//                    Device device = deviceResponse.getData();
//                    Log.i(TAG, "GOT DEVICE!");
//
//                    DHResponse<DeviceCommand> response = device.sendCommand("TEST CMD", new ArrayList<>());
//                    if (response.isSuccessful()) {
//                        Log.i(TAG, "CMD SUCCESS");
//                    } else {
//                        Log.i(TAG, "CMD FAILURE");
//                    }
//
//                    // moved to async
//                    for (int i = 0; i < 1000; i++) {
////            test sending notification
//                        DHResponse<DeviceNotification> rres = device.sendNotification("TEST NOTI",
//                                Collections.singletonList(AnalyticUtils.createTestParam("Test Noti", "Test_" + i,
//                                        AnalyticUtils.generateWearableFilename(device.getName(), "Mobile"))));
//                        if (rres.isSuccessful()) {
//                            Log.i(TAG, "NOTI SUCCESS");
//                        } else {
//                            Log.i(TAG, "CMD FAILURE");
//                        }
//
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException ie) {
//                            Log.w(TAG, "Caught interrupt: " + ie.getMessage());
//                        }
//                    }
//                } else {
//                    Log.i(TAG, "Couldn't get device!");
//                    FailureData failureData = deviceResponse.getFailureData();
//                    int code = failureData.getCode();
//                    String message = failureData.getMessage();
//                    Log.e(TAG, "Failed: " + message);
//                }
            } catch (Exception e) {
                Log.d(TAG, "Exception hitting server! " + e.getMessage());
            }
        } else {
            Toast.makeText(this, "Missing Parameters! Fill in all fields",
                    Toast.LENGTH_LONG);
        }
    }

    @OnClick(R.id.start)
    public void onClick() {
        boolean doProcess = true;

        // send test
//        sendDhTest();

        // predict
        if (activeState == STREAM_STATE && doProcess) {
            Log.d(TAG, "STREAM");
            streamCamera();
        } else if (activeState == CAMERA_STATE && doProcess) {
            Log.d(TAG, "CAMERA");
            predictCamera();
        } else if (activeState == FILE_STATE && doProcess) {
            Log.d(TAG, "FILE");
            boolean granted = isReadStoragePermissionGranted();
            if (granted) {
                loadFiles();
            }
        } else {
            Log.w(TAG, "Invalid active state: " + activeState);
        }

    }

    private void initWebsocketClient() {

    }

//    @OnClick(R.id.start2)
//    public void onExClick() {
//        Log.i(TAG, "Launching example activity");
////        getApplicationContext().startActivity(new Intent(this, Example2.class));
//    }

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
        Intent i = new Intent(this, LiveVideoBroadcasterActivity.class);
        i.putExtra(getString(R.string.key_rtmp), rtspUrl.getText().toString());
        startActivity(i);

//        startCamera();
//
//        mCameraHandler.post(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });
    }

    private void streamFile(File f)
    {
        Log.i(TAG, "streamFile: " + f.getName());


    }

    private void predictCamera() {
        startCamera();

        mCameraHandler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "Request permissions result: " + requestCode);

        switch(requestCode)
        {
            // Reading storage to list files that have been downloaded
            case READ_PERMS:
                loadFiles();
                break;

            // writing media streams to file
            case WRITE_PERMS:

                break;

            // use camera to stream to media server
            case CAMERA_PERMS:
                // call camera method

                break;
            default:
                Log.w(TAG, "Invalid request code: " + requestCode);

        }
    }

    private Map<String, File> listFiles(String name, File dir)
    {
        Map<String, File> ret = new HashMap<>();
        if (dir == null) return ret;

        Log.d(TAG, name + ": " + dir.getAbsolutePath());
        try
        {
            for (File f : dir.listFiles())
            {
                if (f.isDirectory()) {
                    ret.putAll(listFiles(f.getName().replaceAll(" ", "_"), f));
                } else {
                    Log.d(TAG, "file: " + f.getName());
                    if (f.getName().toLowerCase().endsWith("mp4")) {
                        final String trimFile = f.getName().replaceAll(" ", "_");
                        final String fname = (name.isEmpty() ? trimFile : name +"-"+ trimFile);
                        ret.put(fname, f);
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }

        return ret;
    }



    public void loadFiles()
    {

        if (!PSIAPUtils.checkString(rtspUrl.getText().toString())) {
            Log.i(TAG, "Failed RSTP URL check: " + rtspUrl.getText().toString());
            Toast.makeText(this, "Enter a valid RTMP URL", Toast.LENGTH_LONG);
        }

        // get internal storage path
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + Environment.DIRECTORY_DOWNLOADS);
        Map<String, File> fileMap = listFiles("", dir);
        final String[] chars = fileMap.keySet().toArray(new String[0]);

        final CharSequence[] items = new CharSequence[]{"one", "two", "three"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1); // simple_list_item_1
        adapter.addAll(chars);

        // create menu dialog
        AlertDialog a = null;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select video");

        builder.setAdapter(adapter, (DialogInterface dialog, int i) -> { });

        String key = null;
        builder.setSingleChoiceItems(chars, -1, (DialogInterface dialog, int which) -> {
           Log.d(TAG, "Selected item from scitems: "+ which);
           Log.d(TAG, "Item sci val: " + chars[which]);
           // launch stream with file
           File f = fileMap.get(chars[which]);
           if (f != null) {
               // dismiss dialog
               dialog.dismiss();

               // call stream
               streamFile(f);
               Intent i = new Intent(this, VideoBroadcasterActivity.class);
               i.putExtra(getString(R.string.key_media_filepath), f.getAbsolutePath());
               i.putExtra(getString(R.string.key_rtmp), rtspUrl.getText().toString());
               startActivity(i);
           }
        });

        a = builder.create();
        a.show();
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

    @Override
    protected void onResume() {
        super.onResume();

        PreferencesHelper helper = PreferencesHelper.getInstance();

        Log.i(TAG, "DeviceId: " + helper.getDeviceId());
        Log.i(TAG, "Host: " + helper.getServerUrl());

        deviceId.setText(helper.getDeviceId());
        accessToken.setText(helper.getAccessToken());
        refreshToken.setText(helper.getRefreshToken());
        serverAddress.setText(helper.getServerUrl());
        rtspUrl.setText(helper.getRtspUrl());

        deviceId.addTextChangedListener(TextWatcherCreator.getWatcher(deviceIdLayout));
        serverAddress.addTextChangedListener(TextWatcherCreator.getWatcher(serverAddressLayout));
        accessToken.addTextChangedListener(TextWatcherCreator.getWatcher(accessTokenLayout));
        refreshToken.addTextChangedListener(TextWatcherCreator.getWatcher(refreshTokenLayout));
        rtspUrl.addTextChangedListener(TextWatcherCreator.getWatcher(rtspUrlLayout));

        initWebsocketClient();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "OnPause called");
        PreferencesHelper helper = PreferencesHelper.getInstance();

        helper.putDeviceId(deviceId.getText().toString());
        helper.putServerUrl(serverAddress.getText().toString());
        helper.putAccessToken(accessToken.getText().toString().trim());
        helper.putRefreshToken(refreshToken.getText().toString().trim());
        helper.putRtspUrl(rtspUrl.getText().toString().trim());

        if (dhWsClient != null) {
            dhWsClient.closeAndShutdown();
        }

        super.onPause();
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

    public  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Read permission is granted");
                return true;
            } else {

                Log.v(TAG,"Read permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMS);
                return false;
            }
        }
        else { // permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Read permission is granted");
            return true;
        }
    }

    public  boolean isWriteStoragePermissionGranted(final int code) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Write permission is granted");
                return true;
            } else {

                Log.v(TAG,"Write permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, code);
                return false;
            }
        }
        else { // permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Write permission is granted");
            return true;
        }
    }

}
