package com.openquartz.helloglass;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {
    private Camera camera = null;
    private ClientSocket socket = null;
    //private TextToSpeech textToSpeech = null;
    ExecutorService executor = Executors.newFixedThreadPool(10);

    public CameraView(Context context) {
        super(context);

        final SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        socket = new ClientSocket();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
//        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                if (status == TextToSpeech.SUCCESS) {
//                    int result = textToSpeech.setLanguage(Locale.ENGLISH);
//                    if (result == TextToSpeech.LANG_MISSING_DATA ||
//                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                        ;
//                    } else {
//                        speak();
//                    }
//                }
//            }
//        });
        // Set the Hotfix for Google Glass
        setCameraParameters(camera);

        // Show the Camera display
        try {
            camera.setPreviewDisplay(holder);
        } catch (Exception e) {
            releaseCamera();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Start the preview for surfaceChanged
        if (camera != null) {
            camera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Do not hold the camera during surfaceDestroyed - view should be gone
        releaseCamera();
    }

    /**
     * Important HotFix for Google Glass (post-XE11) update
     *
     * @param camera Object
     */
    public void setCameraParameters(Camera camera) {
        if (camera != null) {
            final Parameters parameters = camera.getParameters();
            parameters.setPreviewFpsRange(30000, 30000);
            camera.setParameters(parameters);
        }
    }

    /**
     * TODO: Adding picture taking options to CameraView
     */

    public void takePicture()
    {

        Camera.PictureCallback previewPicture = new Camera.PictureCallback()
        {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                System.out.println("postview");
                try {
//                    System.out.println(bytes);
                    System.out.println(bytes.length);
                    socket.sendBytes(bytes);
                    camera.startPreview();
                } catch (NullPointerException e) {
                    //Log.v(TAG, e.getMessage());
                } catch (Exception e){

                }
            }
        };
        Camera.PictureCallback rawPicture = new Camera.PictureCallback()
        {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                System.out.println("raw");
                try {
                    if(bytes != null)
                    {
                        try
                        {

                            System.out.println(bytes);
                            System.out.println(bytes.length);
                            socket.sendBytes(bytes);
                        }
                        catch (Exception e){

                        }
                    }
                    camera.startPreview();
                } catch (NullPointerException e) {
                        Log.v(TAG, e.getMessage());
                }
            }
        };
        Camera.PictureCallback picture = new Camera.PictureCallback()
        {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                System.out.println("jpeg");
//                Log.v(TAG, "Getting output media file");
//                File pictureFile = getOutputMediaFile();
//                if (pictureFile == null) {
//                    Log.v(TAG, "Error creating output file");
//                    return;
//                }
                try {
//                    FileOutputStream fos = new FileOutputStream(pictureFile);
//                    fos.write(bytes);
//                    System.out.println(pictureFile);
                    System.out.println(bytes);
                    System.out.println(bytes.length);

                    socket.sendBytes(bytes);

//                    fos.close();
                    camera.startPreview();
//                } catch (FileNotFoundException e) {
//                    Log.v(TAG, e.getMessage());
                } catch (IOException e) {
                    Log.v(TAG, e.getMessage());
                } catch(NullPointerException e){
                    Log.v(TAG, e.getMessage());
                }
            }
        };
        try {
            camera.startPreview();
            camera.takePicture(null, null, previewPicture, null);



        } catch (Exception e) {
            Log.v(TAG, e.getMessage());
        }

    }

    private static File getOutputMediaFile() {
//        String state = Environment.getExternalStorageState();
//        if (!state.equals(Environment.MEDIA_MOUNTED)) {
//            return null;
//        }
//        else {
//            File folder_gui = new File(Environment.getExternalStorageDirectory() + File.separator + "GUI");
//            if (!folder_gui.exists()) {
//                Log.v(TAG, "Creating folder: " + folder_gui.getAbsolutePath());
//                folder_gui.mkdirs();
//            }
//            File outFile = new File(folder_gui, "temp.jpg");
//            Log.v(TAG, "Returning file: " + outFile.getAbsolutePath());
//            return outFile;
//        }

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"DanielTest3");
        Log.v(TAG, mediaStorageDir.toString());
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {

                return null;
            }
        }

        File mediaFile = new File(String.format(mediaStorageDir+File.separator+"%d.jpg", System.currentTimeMillis()));

        return mediaFile;


    }

//    private void speak() {
//        String text = editText.getText().toString();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
//        } else {
//            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
//        }
//    }




    /**
     * Release the camera from use
     */
    public void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

}
