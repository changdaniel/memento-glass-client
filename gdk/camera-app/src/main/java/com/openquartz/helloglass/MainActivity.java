package com.openquartz.helloglass;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;


import android.widget.ImageView;

import com.google.android.glass.content.Intents;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;


import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.io.File;

import static android.content.ContentValues.TAG;


public class MainActivity extends Activity {

    private Socket socket = new Socket();
    private static final int SERVERPORT = 8088;
    private static final String SERVER_IP = "3.134.84.232"; //EC2 Public


    private static final int TAKE_PICTURE_REQUEST = 1;
    private static final int TAKE_VIDEO_REQUEST = 2;
    private GestureDetector mGestureDetector = null;
    private CameraView cameraView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initiate CameraView
        cameraView = new CameraView(this);

        // Turn on Gestures
        mGestureDetector = createGestureDetector(this);

        // Set the view
        setContentView(cameraView);


        new Thread(new ClientThread()).start();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Do not hold the camera during onResume
        if (cameraView != null) {
            cameraView.releaseCamera();
        }

        // Set the view
        setContentView(cameraView);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Do not hold the camera during onPause
        if (cameraView != null) {
            cameraView.releaseCamera();

        }
    }

    /**
     * Gesture detection for fingers on the Glass
     */
    private GestureDetector createGestureDetector(Context context) {
        final GestureDetector gestureDetector = new GestureDetector(context);

        //Create a base listener for generic gestures
        gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
            @Override
            public boolean onGesture(Gesture gesture) {
                // Make sure view is initiated
                if (cameraView != null) {
                    // Tap with a single finger for photo
                    if (gesture == Gesture.TAP) {

                        try{



                            final String str = "test";
                            final PrintWriter out = new PrintWriter(new BufferedWriter(
                                new OutputStreamWriter(socket.getOutputStream())),
                                true);
                            System.out.println(str);

                            new Thread(new Runnable() {
                                public void run() {
                                    try {
                                        out.write(str);

                                        out.flush();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.i(TAG, "SendDataToNetwork: Message send failed. Caught an exception");
                                    }
                                }
                            }).start();

                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

//                        for (int i = 0; i < 5; i ++)
//                        {
//                            try
//                            {
//                                Thread.sleep(5000);
//                                cameraView.takePicture();
//                            }
//                            catch(Exception e)
//                            {
//                                Log.v(TAG, e.getMessage());
//                            }
//                        }

                        return true;
                    }
//                    } else if (gesture == Gesture.TWO_TAP) {
//                        // Tap with 2 fingers for video
//                        startActivityForResult(new Intent(MediaStore.ACTION_VIDEO_CAPTURE),
//                            TAKE_VIDEO_REQUEST);
//
//                        return true;
//                    }
                }

                return false;
            }
        });

        return gestureDetector;
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        // Send generic motion events to the gesture detector
        return mGestureDetector != null && mGestureDetector.onMotionEvent(event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Handle photos
        if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK)
        {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            //System.out.println(bitmap);
//            String picturePath = data.getStringExtra(Intents.EXTRA_PICTURE_FILE_PATH);
//            processPictureWhenReady(picturePath);
        }
        else if (resultCode != RESULT_OK)
        {
            //System.out.println("Picture not confirmed");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Process picture - from example GDK
     */
    private void processPictureWhenReady(final String picturePath) {
        final File pictureFile = new File(picturePath);

        if (pictureFile.exists()) {
            // The picture is ready; process it.

            ;
        } else {
            // The file does not exist yet. Before starting the file observer, you
            // can update your UI to let the user know that the application is
            // waiting for the picture (for example, by displaying the thumbnail
            // image and a progress indicator).
            final File parentDirectory = pictureFile.getParentFile();
            final FileObserver observer = new FileObserver(parentDirectory.getPath()) {
                // Protect against additional pending events after CLOSE_WRITE is
                // handled.
                private boolean isFileWritten;

                @Override
                public void onEvent(int event, String path) {
                    if (!isFileWritten) {
                        // For safety, make sure that the file that was created in
                        // the directory is actually the one that we're expecting.
                        final File affectedFile = new File(parentDirectory, path);
                        isFileWritten = (event == FileObserver.CLOSE_WRITE
                            && affectedFile.equals(pictureFile));

                        if (isFileWritten) {
                            stopWatching();

                            // Now that the file is ready, recursively call
                            // processPictureWhenReady again (on the UI thread).
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    processPictureWhenReady(picturePath);
                                }
                            });
                        }
                    }
                }
            };
            observer.startWatching();
        }
    }

    class ClientThread implements Runnable {

        @Override
        public void run() {

            try {
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

                socket = new Socket(serverAddr, SERVERPORT);

            } catch (UnknownHostException e1) {
                e1.printStackTrace();
            } catch (IOException     e1) {
                e1.printStackTrace();
            }

        }

    }

}
