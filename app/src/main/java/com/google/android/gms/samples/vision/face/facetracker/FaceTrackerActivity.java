/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.face.facetracker;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.face.facetracker.ui.camera.GraphicOverlay;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Activity for the face tracker app.  This app detects faces with the rear facing camera, and draws
 * overlay graphics to indicate the position, size, and ID of each face.
 */
public final class FaceTrackerActivity extends AppCompatActivity {
    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource = null;

    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;

    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private Button pauseButton;
    private Button welcomeButton;
    private Button redo;
    private Button exit;
    private View gameView;
    private View welcome;
    private View finishView;
    private TextView test;
    private ImageView instruction_circle;
    private ImageView cursor;
    private ImageView soundOn;
    private ImageView soundOff;
    private ImageSwitcher imageSwitcher;
    private game game = new game(FaceTrackerActivity.this);
    private SoundPool soundPool;
    private MediaPlayer mp = null;

    private Integer[] images = {R.drawable.left,R.drawable.right,R.drawable.front,R.drawable.front};
    private boolean lock = false;   // lock sound play in step 4 for 1 second each time
    private boolean step1_lock = false; // false -> can play
    private boolean step2_lock = true;  // ture  -> cannot play
    private boolean step3_lock = true;
    private boolean step4_lock = true;
    private boolean step4_inst_lock = true;
    private boolean pause = true;
    private boolean count_lock = false;  // true -> <int>count locked
    private boolean back = true;
    private float x;
    private float y;
    private int count = 0;
    private int step = 0;

    private Handler handler = new Handler() {
        public void handleMessage (Message msg) {
            if (msg.what == 1) {

                cursor.setX(x);
                cursor.setY(y);

                test.setText("\nStep " + (step+1) + ", count " + count);

                //=================================================
                // Count 'count' and 'step'
                //-------------------------------------------------
                switch (step) {
                    case 0:
                        if (pause == false) {
                            if (step1_lock == false) {  // play instruction
                                step1_lock = true;
                                count_lock = true;
                                soundPool.play(7,1,1,1,0,1);
                                step2_lock = false;
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        count_lock = false;
                                    }
                                },2500); // wait after finishing playing step 1 instruction
                            }
                        }

                        if (back == true && x>150 && x<270 && y>400 && y<520) {
                            if (pause == false) back = false;
                        }
                        if (back == false && x>270) {
                            back = true;
                            if(count_lock == false) {
                                soundPool.play(count+1,1,1,1,0,1);  // soundId == count+1
                                count++;
                            }
                            if (count == 5) {
                                Handler _handler = new Handler();
                                _handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        soundPool.play(6,1,1,1,0,1);
                                    }
                                },500);
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        step++;
                                        count = 0;
                                    }
                                },1500); // wait after finishing playing 'well down'
                            }
                        }
                        break;
                    case 1:
                        if (step2_lock == false) {  // play instruction
                            step2_lock = true;
                            count_lock = true;
                            soundPool.play(8,1,1,1,0,1);
                            step3_lock = false;
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    count_lock = false;
                                }
                            },2500);
                        }

                        if (back == true && x>380 && x<510 && y>400 && y<520) {
                            if (pause == false) back = false;
                        }
                        if (back == false && x<380) {
                            back = true;
                            if(count_lock == false) {
                                soundPool.play(count+1,1,1,1,0,1);
                                count++;
                            }
                            if (count == 5) {
                                Handler _handler = new Handler();
                                _handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        soundPool.play(6,1,1,1,0,1);
                                    }
                                },500);
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        step++;
                                        count = 0;
                                    }
                                },1500);
                            }
                        }
                        break;
                    case 2:
                        if (step3_lock == false) {  // play instruction
                            step3_lock = true;
                            count_lock = true;
                            soundPool.play(9,1,1,1,0,1);
                            step4_lock = false;
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    count_lock = false;
                                }
                            },2500);
                        }

                        if (back == true && x>260 && x<380 && y>270 && y<390) {
                            if (pause == false) back = false;
                        }
                        if (back == false && y>390) {
                            back = true;
                            if(count_lock == false) {
                                soundPool.play(count+1,1,1,1,0,1);
                                count++;
                            }
                            if (count == 5) {
                                Handler _handler = new Handler();
                                _handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        soundPool.play(6,1,1,1,0,1);
                                    }
                                },500);
                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        step++;
                                        count = 0;
                                    }
                                },1500);
                            }
                        }
                        break;
                    case 3:
                        if (step4_lock == false) {  // play instruction
                            step4_lock = true;
                            count_lock = true;
                            soundPool.play(10,1,1,1,0,1);
                            step1_lock = false;
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    count_lock = false;
                                }
                            },4000);
                        }

                        if (back == true) {
                            if (pause == false) {
                                if (x>260 && x<380 && y>555 && y<675) {
                                    step4_inst_lock = false;
                                    if (count < 3 && lock == false && count_lock == false) {
                                        lock = true;
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (x>260 && x<380 && y>555 && y<675){
                                                    soundPool.play(count+1,1,1,1,0,1);
                                                    count++;
                                                    lock = false;
                                                }
                                            }
                                        },1000);
                                    } else if (count == 3) {
                                        back = false;
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                soundPool.play(6,1,1,1,0,1);
                                                step4_inst_lock = true;
                                            }
                                        },500);
                                    }
                                } else { // out of circle range
                                    if (count < 3) {
                                        lock = false;
                                        count = 0;
                                        if (step4_inst_lock == false) { // play instruction:"Please keep heading down"
                                            soundPool.play(11,1,1,1,0,1);
                                            step4_inst_lock = true;
                                        }
                                    }
                                }
                            }
                        }
                        if (back == false && y<555) {
                            back = true;
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finishView.setVisibility(View.VISIBLE);
                                    pause = true;
                                    step = 0;
                                    count = 0;
                                }
                            },500);
                        }
                        break;
                    default:
                }
                //=================================================
                // Paint background for each step
                //-------------------------------------------------
                Paint paint = new Paint();
                Bitmap bmp = Bitmap.createBitmap(720,1086,Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bmp);

                if(step == 0) {
                    paint.setColor(Color.BLUE);
                    canvas.drawCircle(bmp.getWidth()/3,bmp.getHeight()*3/7,60,paint);
                } else if(step == 1) {
                    paint.setColor(Color.GREEN);
                    canvas.drawCircle(bmp.getWidth()*2/3,bmp.getHeight()*3/7,60,paint);
                } else if(step == 2) {
                    paint.setColor(Color.YELLOW);
                    canvas.drawCircle(bmp.getWidth()/2,bmp.getHeight()/3,60,paint);
                } else {
                    paint.setColor(Color.RED);
                    canvas.drawCircle(bmp.getWidth()/2,bmp.getHeight()*3/5,60,paint);
                }
                instruction_circle.setImageBitmap(bmp);
                instruction_circle.setAlpha(0.5f);  // transparent
                imageSwitcher.setImageResource(images[step]);
                //=================================================
            }
        }
    };

    //==============================================================================================
    // Activity Methods
    //==============================================================================================

    /**
     * Initializes the UI and initiates the creation of a face detector.
     */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        pauseButton = (Button) findViewById(R.id.pauseButton);
        welcomeButton = (Button) findViewById(R.id.welcomeButton);
        redo = (Button) findViewById(R.id.redo);
        exit = (Button) findViewById(R.id.exit);
        gameView = findViewById(R.id.gameView);
        welcome = findViewById(R.id.welcome);
        finishView = findViewById(R.id.finishView);
        test = (TextView) findViewById(R.id.test);
        soundOn = (ImageView) findViewById(R.id.soundOn);
        soundOff = (ImageView) findViewById(R.id.soundOff);

        // Setup sound pool
        AudioAttributes aa = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA).build();
        soundPool = new SoundPool.Builder().setMaxStreams(5)
                .setAudioAttributes(aa).build();

        soundPool.load(getApplicationContext(),R.raw.one,1);    // soundId = 1
        soundPool.load(getApplicationContext(),R.raw.two,1);    // soundId = 2
        soundPool.load(getApplicationContext(),R.raw.three,1);  // soundId = 3
        soundPool.load(getApplicationContext(),R.raw.four,1);   // soundId = 4
        soundPool.load(getApplicationContext(),R.raw.five,1);   // soundId = 5
        soundPool.load(getApplicationContext(),R.raw.well_done,1);  // soundId = 6
        soundPool.load(getApplicationContext(),R.raw.left_five,1);  // soundId = 7
        soundPool.load(getApplicationContext(),R.raw.right_five,1);  // soundId = 8
        soundPool.load(getApplicationContext(),R.raw.up_five,1);  // soundId = 9
        soundPool.load(getApplicationContext(),R.raw.down_three_s,1);  // soundId = 10
        soundPool.load(getApplicationContext(),R.raw.down_instruction,1);  // soundId = 11

        // Setup image switcher
        imageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            @Override
            public View makeView() {
                ImageView imageView = new ImageView(getApplicationContext());
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imageView.setLayoutParams(
                        new ImageSwitcher.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT));
                return imageView;
            }
        });
        imageSwitcher.setImageResource(images[step]);

        instruction_circle = (ImageView) findViewById(R.id.instruction_circle);
        cursor = (ImageView) findViewById(R.id.cursor);

        // Setup mediaPlayer for background music
        mp = MediaPlayer.create(getApplicationContext(),R.raw.background);
        mp.setLooping(true);
        mp.setVolume(0.2f,0.2f);

        welcomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                welcomeButton.setEnabled(false);
                welcome.setVisibility(View.GONE);
                pauseButton.setEnabled(true);
                if(!mp.isPlaying())
                    mp.start();
            }
        });

        //game.setButtonListener(pauseButton2,testText);

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pauseButton.getText() == "Pause") { // In Game -> Pause
                    gameView.setVisibility(View.GONE);
                    pauseButton.setText("Resume");
                    pause = true;
                } else { // Pause -> In Game
                    gameView.setVisibility(View.VISIBLE);
                    pauseButton.setText("Pause");
                    pause = false;
                }
            }
        });

        soundOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundOff.setVisibility(View.GONE);
                soundOn.setVisibility(View.VISIBLE);
                if(mp.isPlaying())
                    mp.pause();
            }
        });

        soundOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                soundOn.setVisibility(View.GONE);
                soundOff.setVisibility(View.VISIBLE);
                if(!mp.isPlaying())
                    mp.start();
            }
        });

        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishView.setVisibility(View.GONE);
                pause = false;
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mp.isPlaying())
                    mp.stop();
                finish();
            }
        });
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {

        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(30.0f)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();

        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("FaceTracker Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(mGraphicOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);

            x = mFaceGraphic.get_X();
            y = mFaceGraphic.get_Y();
            game.setCoordinates(x,y,handler);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);

            x = mFaceGraphic.get_X();
            y = mFaceGraphic.get_Y();
            game.setCoordinates(x,y,handler);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }


    }
}
