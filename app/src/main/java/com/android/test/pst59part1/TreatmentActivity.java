package com.android.test.pst59part1;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class TreatmentActivity extends AppCompatActivity {

    protected Uri imageUri;
    protected ZoomableImageView treatableImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment);
        imageUri = getIntent().getParcelableExtra("uri");
        treatableImageView = findViewById(R.id.image);
        treatableImageView.setImageURI(imageUri);
    }
}
