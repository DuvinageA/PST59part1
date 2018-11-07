package com.android.test.pst59part1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;
import java.util.Objects;

public class DescriptionDialogFragment extends AppCompatDialogFragment {

    private TreatedImage treatedImage;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        TreatmentActivity activity = (TreatmentActivity)Objects.requireNonNull(getActivity());
        treatedImage = activity.treatedImage;
        int currentSelection = activity.selectableImageView.getCurrentSelection();
        final RectF selection = treatedImage.getSelection(currentSelection);
        String text = treatedImage.getDescription().get(selection);
        Uri imageUri = treatedImage.getImageUri();
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
            Matrix matrix = new Matrix();
            matrix.setRotate(-90f);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap = Bitmap.createBitmap(bitmap, (int)selection.left, (int)selection.top, (int)selection.width(), (int)selection.height());
        } catch (IOException e) {
            e.printStackTrace();
        }
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.content_dialog_fragment, null);
        ImageView imageView = layout.findViewById(R.id.imageView);
        final EditText editText = layout.findViewById(R.id.editText);
        FloatingActionButton floatingActionButton = layout.findViewById(R.id.floatingActionButton);
        builder.setTitle("Description de la sélection");
        builder.setView(layout);
        builder.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                treatedImage.getDescription().replace(selection, editText.getText().toString());
            }
        });
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        Dialog dialog = builder.create();
        imageView.setImageDrawable(bitmapDrawable);
        if (text != null) {
            editText.setText(text);
        }
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return dialog;
    }
}
