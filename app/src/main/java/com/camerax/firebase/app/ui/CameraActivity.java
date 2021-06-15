package com.camerax.firebase.app.ui;

import android.animation.ObjectAnimator;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.camerax.firebase.app.R;
import com.camerax.firebase.app.databinding.ActivityCameraBinding;
import com.camerax.firebase.app.utils.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class CameraActivity extends AppCompatActivity {


    ActivityCameraBinding binding;

    private TextureView cameraView;
    private File file =null;

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private ObjectAnimator anim;
    private Boolean taskStatus = false;

    private String dateAndTime;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        binding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        initView();
    }

    private void initView() {
        cameraView = binding.cameraView;

        //Initiating Firebase Storage and Database
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        storageRef = FirebaseStorage.getInstance().getReference();

        binding.backBtn.setOnClickListener(v -> {
            CameraX.unbindAll(); //Unbinding CameraX if its running on thread
            finish();
        });

        if (Util.INSTANCE.permissionGranted(this)){
            /// Starting the CameraX
            startCamera();
        }else {
           Util.INSTANCE.requestPermission(this);
        }
    }



    private void startCamera() {
        CameraX.unbindAll(); //Unbinding CameraX if its running on thread

        //Get the aspectRatio and Size of the TextureView
        Rational aspectRatio = new Rational(cameraView.getWidth(),cameraView.getHeight());
        Size screen =new Size(cameraView.getWidth(),cameraView.getHeight());


        //Preview Configuration on the target TextureView
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setTargetAspectRatio(aspectRatio)
                .setTargetResolution(screen)
                .build();

        Preview preview = new Preview(previewConfig);

        //Raw output
        preview.setOnPreviewOutputUpdateListener(output -> {
            ViewGroup parent = (ViewGroup) cameraView.getParent();
            parent.removeView(cameraView);
            parent.addView(cameraView);

            cameraView.setSurfaceTexture(output.getSurfaceTexture());

            updateTransformation();
        });

        /*Image Capture Config, to make the raw data process faster.*/
        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                .build();
        ImageCapture imageCapture = new ImageCapture(imageCaptureConfig);

        binding.captureBtn.setOnClickListener(v -> {

            /*Storing the image in device for a short amount of time,
            it will help to ready the file for uploading to Firestore*/

            file = Util.INSTANCE.getFile();

            startInfiniteRotatingAnim(binding.captureBtn);

            imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
                @Override
                public void onImageSaved(@NonNull @NotNull File file) {
                    uploadCurrentImage(); //Upload Function
                }

                @Override
                public void onError(@NonNull @NotNull ImageCapture.UseCaseError useCaseError, @NonNull @NotNull String message, @Nullable @org.jetbrains.annotations.Nullable Throwable cause) {
                    if (cause!=null){
                        cause.printStackTrace();
                    }
                }
            });
        });

        CameraX.bindToLifecycle(this,preview,imageCapture);
    }

    private void updateTransformation() {
        Matrix mx = new Matrix();
        float w = cameraView.getWidth();
        float h = cameraView.getHeight();

        float cx = w/2f;
        float cy = h/2f;

        int rotationDgr;
        int rotation = (int)cameraView.getRotation();

        switch (rotation){
            case Surface.ROTATION_0:
                rotationDgr =0;
                break;
            case Surface.ROTATION_90:
                rotationDgr =90;
                break;
            case Surface.ROTATION_180:
                rotationDgr =180;
                break;
            case Surface.ROTATION_270:
                rotationDgr =270;
                break;
            default:
                return;
        }
        mx.postRotate((float)rotationDgr,cx,cy);
        cameraView.setTransform(mx);
    }


    private void uploadCurrentImage(){
        if (file!=null){

            //Cooking the Name and date
            dateAndTime = Util.INSTANCE.getDateAndTime();
            fileName = "Lawnics"+dateAndTime;

            //Direct upload to CloudStorage
            storageRef.child("Images")
                    .child(fileName).putFile(Uri.fromFile(file)).addOnSuccessListener(taskSnapshot -> {
                        if (taskSnapshot!=null){
                            taskSnapshot
                                    .getMetadata()
                                    .getReference()
                                    .getDownloadUrl()
                                    .addOnSuccessListener(uri -> {

                                        //Record the uploaded file to firebase CloudStore
                                        uploadDataToFirestore(
                                                fileName,
                                                uri.toString(),
                                                dateAndTime);
                                        deleteCurrentFile();
                                    });
                        }
                    }).addOnFailureListener(e -> stopAnim());
        }

    }

    private void uploadDataToFirestore(String fileName,String imageUrl,String date){

        Map<String,String> dataMap = new HashMap<>();
        dataMap.put("title",fileName);
        dataMap.put("imageUrl",imageUrl);
        dataMap.put("date",dateAndTime);

        firestore.collection("ImageData") //Firestore Path Name, make sure that this pathname exists into database
                .add(dataMap)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getBaseContext(),"Image Uploaded Successfully!",Toast.LENGTH_LONG).show();
                    stopAnim();
                    Log.d("DB","DATA UPLOADED SUCCESSFULLY TO FIREBASE");

                }).addOnFailureListener(e -> {
                    stopAnim();
                    Log.e("DB",e.getMessage());
        });
    }

    /*After uploading the current image it will delete the file from the device*/
    private Boolean deleteCurrentFile(){
        if (file.delete()){
            Log.d("FILE","Task finished");
            return true;
        }
        return false;
    }

    /*Rotating animation  of the capture button*/
    private void startInfiniteRotatingAnim(View view){
        anim = ObjectAnimator.ofFloat(view, "rotation", 0, 360);
        anim.setDuration(1000);
        anim.setRepeatCount(Animation.INFINITE);
        anim.start();
        binding.captureBtn.setEnabled(false);
    }

    private void stopAnim(){
        anim.end();
        binding.captureBtn.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        CameraX.unbindAll();
        finish();
    }


}
