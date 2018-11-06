package com.android.test.pst59part1;

import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class TreatmentActivity extends AppCompatActivity implements SelectableImageView.OnEditingModeListener {

    protected Uri imageUri;
    protected SelectableImageView treatableImageView;
    protected FloatingActionButton temporaryFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment);
        imageUri = getIntent().getParcelableExtra("uri");
        treatableImageView = findViewById(R.id.image);
        treatableImageView.setImageURI(imageUri);
        temporaryFab = findViewById(R.id.fab);
        temporaryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                treatableImageView.quitEditingMode();
                temporaryFab.setVisibility(View.INVISIBLE);
            }
        });
        temporaryFab.setVisibility(View.INVISIBLE);
    }

    @Override
    public void notifyEditingModeChange(SelectableImageView.Mode mode) {
        if (mode == SelectableImageView.Mode.DISPLAYING) {
            temporaryFab.setVisibility(View.INVISIBLE);
        } else if (mode == SelectableImageView.Mode.EDITING) {
            temporaryFab.setVisibility(View.VISIBLE);
        }
    }
}
