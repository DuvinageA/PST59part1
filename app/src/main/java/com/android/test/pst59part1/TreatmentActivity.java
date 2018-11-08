package com.android.test.pst59part1;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class TreatmentActivity extends AppCompatActivity implements SelectableImageView.SelectableImageViewListener {

    protected TreatedImage treatedImage;
    protected SelectableImageView selectableImageView;
    protected FloatingActionButton temporaryFab;
    protected Menu menu;
    protected int imageHeight;
    protected int imageWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treatment);
        treatedImage = getIntent().getParcelableExtra("image");
        selectableImageView = findViewById(R.id.image);
        selectableImageView.setImageURI(treatedImage.getImageUri());
        selectableImageView.setTreatedImage(treatedImage);
        temporaryFab = findViewById(R.id.fab);
        temporaryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (treatedImage.getDescription().get(treatedImage.getSelection(selectableImageView.getCurrentSelection())) != null) {
                    selectableImageView.quitEditingMode();
                    temporaryFab.setVisibility(View.INVISIBLE);
                } else {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    AppCompatDialogFragment fragment = new DescriptionDialogFragment();
                    fragment.show(fragmentManager, "text_edition");
                }
            }
        });
        temporaryFab.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_treatment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                Intent returnIntent = new Intent();
                returnIntent.putExtra("image", treatedImage);
                setResult(RESULT_OK, returnIntent);
                finish();
                return true;
            case R.id.delete:
                int current = selectableImageView.getCurrentSelection();
                selectableImageView.removeSelection(current);
                selectableImageView.resetCurrentSelection();
                selectableImageView.quitEditingMode();
                return true;
            case R.id.edit:
                FragmentManager fragmentManager = getSupportFragmentManager();
                AppCompatDialogFragment fragment = new DescriptionDialogFragment();
                fragment.show(fragmentManager, "text_edition");
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            menu.findItem(R.id.save).setVisible(true);
            menu.findItem(R.id.delete).setVisible(false);
            menu.findItem(R.id.edit).setVisible(false);
        } else if (mode == SelectableImageView.Mode.EDITING) {
            temporaryFab.setVisibility(View.VISIBLE);
            menu.findItem(R.id.save).setVisible(false);
            menu.findItem(R.id.delete).setVisible(true);
            menu.findItem(R.id.edit).setVisible(true);
        }
    }

    @Override
    public void declareImageSize(int height, int width) {
        imageHeight = height;
        imageWidth = width;
    }
}
