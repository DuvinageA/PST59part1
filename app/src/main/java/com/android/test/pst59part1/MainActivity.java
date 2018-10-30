package com.android.test.pst59part1;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Choisissez une image");
                builder.setAdapter(
                        new ListAdapter() {
                            @Override
                            public boolean areAllItemsEnabled() {
                                return true;
                            }

                            @Override
                            public boolean isEnabled(int position) {
                                return (position == 1 || position == 0);
                            }

                            @Override
                            public void registerDataSetObserver(DataSetObserver observer) {

                            }

                            @Override
                            public void unregisterDataSetObserver(DataSetObserver observer) {

                            }

                            @Override
                            public int getCount() {
                                return 2;
                            }

                            @Override
                            public Object getItem(int position) {
                                return null;
                            }

                            @Override
                            public long getItemId(int position) {
                                return 0;
                            }

                            @Override
                            public boolean hasStableIds() {
                                return false;
                            }

                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                View view = convertView;
                                if (view == null) {
                                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                                    view = inflater.inflate(R.layout.content_choose_photo, parent, false);
                                }
                                ImageView image = view.findViewById(R.id.row_imageView);
                                TextView text = view.findViewById(R.id.row_textView);
                                switch (position) {
                                    case 0:
                                        image.setImageDrawable(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.ic_menu_camera));
                                        text.setText("Appareil photo");
                                        break;
                                    case 1:
                                        image.setImageDrawable(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.ic_menu_gallery));
                                        text.setText("Galerie");
                                        break;
                                }
                                return view;
                            }

                            @Override
                            public int getItemViewType(int position) {
                                return 0;
                            }

                            @Override
                            public int getViewTypeCount() {
                                return 1;
                            }

                            @Override
                            public boolean isEmpty() {
                                return false;
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendRequest(which);
                            }
                        }
                );
                builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Intent intent = new Intent(this, TraitementActivity.class);
            if (requestCode == 1) {
                Uri selectedImage = data.getData();
                imageUri = selectedImage;
            }
            intent.putExtra("uri", imageUri);
            startActivity(intent);
        }
    }

    protected void sendRequest(int which) {
        switch (which) {
            case 0:
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePicture.resolveActivity(this.getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) { }
                    if (photoFile != null) {
                        imageUri = FileProvider.getUriForFile(this,
                                "com.android.test.fileprovider",
                                photoFile);
                        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(takePicture, 0);
                    }
                }

                break;
            case 1:
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }
}
