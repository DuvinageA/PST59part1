package com.android.test.pst59part1;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class DescriptionDialogFragment extends AppCompatDialogFragment implements RecognitionListener {

    private TreatedImage treatedImage;
    private EditText editText;

    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final TreatmentActivity activity = (TreatmentActivity)Objects.requireNonNull(getActivity());
        final SpeechRecognizer speech = SpeechRecognizer.createSpeechRecognizer(activity);
        speech.setRecognitionListener(this);
        final Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.FRENCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);

        treatedImage = activity.treatedImage;
        int currentSelection = activity.selectableImageView.getCurrentSelection();
        final RectF selection = treatedImage.getSelection(currentSelection);
        String text = treatedImage.getDescription().get(selection);
        Uri imageUri = treatedImage.getImageUri();
        Bitmap bitmap = null;
        try {
            ExifInterface exif = new ExifInterface(activity.getContentResolver().openInputStream(imageUri));
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), imageUri);
            System.out.println(bitmap.getHeight() + "x" + bitmap.getWidth());
            Matrix matrix = new Matrix();
            if (rotation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.setRotate(90f);
            } else if (rotation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.setRotate(180f);
            } else if (rotation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.setRotate(270f);
            }
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
        editText = layout.findViewById(R.id.editText);
        FloatingActionButton floatingActionButton = layout.findViewById(R.id.floatingActionButton);
        builder.setTitle("Description de la sélection");
        builder.setView(layout);
        builder.setPositiveButton("Valider", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String content = editText.getText().toString();
                treatedImage.getDescription().replace(selection, content.isEmpty() ? null:content);
                activity.temporaryFab.callOnClick();
                dialog.cancel();
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
        floatingActionButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        speech.startListening(recognizerIntent);
                        break;
                    case MotionEvent.ACTION_UP:
                        speech.stopListening();
                        break;
                }
                return false;
            }
        });
        return dialog;
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {

    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> result = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        assert result != null;
        editText.setText(result.get(0));
    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
}
