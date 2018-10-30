package com.android.test.pst59part1;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class TraitementActivity extends AppCompatActivity {

    protected Uri imageUri;
    protected ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traitement);
        imageUri = getIntent().getParcelableExtra("uri");
        imageView = findViewById(R.id.image);
        imageView.setImageURI(imageUri);
    }
}
