package com.android.test.pst59part1;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class TreatmentActivity extends AppCompatActivity implements SelectableImageView.OnEditingModeListener {

    protected TreatedImage treatedImage;
    protected SelectableImageView treatableImageView;
    protected FloatingActionButton temporaryFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment);
        treatedImage = getIntent().getParcelableExtra("image");
        treatableImageView = findViewById(R.id.image);
        treatableImageView.setImageURI(treatedImage.getImageUri());
        treatableImageView.setTreatedImage(treatedImage);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("image", treatedImage);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        treatedImage = savedInstanceState.getParcelable("image");
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
