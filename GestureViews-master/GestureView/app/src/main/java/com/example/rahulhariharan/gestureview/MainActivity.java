package com.example.rahulhariharan.gestureview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;

import com.example.rahulhariharan.gestureview.cropper.CropImage;
import com.example.rahulhariharan.gestureview.cropper.CropImageView;
import com.example.rahulhariharan.gestureview.cropper.CropOverlayView;
import com.example.rahulhariharan.gestureview.gestures.Settings;
import com.example.rahulhariharan.gestureview.gestures.views.GestureFrameLayout;
import com.example.rahulhariharan.gestureview.gestures.views.GestureImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GestureFrameLayout gestureView = findViewById(R.id.gesture_layout);
        gestureView.getController().getSettings()
                .setMaxZoom(2f)
                .setDoubleTapZoom(-1f) // Falls back to max zoom level
                .setPanEnabled(true)
                .setZoomEnabled(true)
                .setDoubleTapEnabled(true)
                .setRotationEnabled(false)
                .setRestrictRotation(false)
                .setOverscrollDistance(0f, 0f)
                .setOverzoomFactor(2f)
                .setFillViewport(false)
                .setFitMethod(Settings.Fit.INSIDE)
                .setGravity(Gravity.CENTER);


        /*GestureFrameLayout frameLayout = findViewById(R.id.cropImageView);
        frameLayout.setGuidelines(CropImageView.Guidelines.ON);
        frameLayout.setFixedAspectRatio(true);
        frameLayout.setOnSetCropOverlayMovedListener(null);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.water);
        frameLayout.setImageBitmap(bitmap);*/
    }
}
