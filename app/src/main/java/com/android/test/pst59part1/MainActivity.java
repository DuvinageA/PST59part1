package com.android.test.pst59part1;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    final static private String ACCESS_TOKEN = "K4DN47ESdsAAAAAAAAAB6MtUjfCbTVcNRBLalSWyoKcX9HVUgUx04lWcOjOt7Bob";
    private static final String IP_ADDRESS = "192.168.1.13";

    private Uri imageUri;
    private ArrayList<TreatedImage> treatedImages;
    private MainActivity activity;
    private DbxClientV2 client;
    private ArrayAdapter<TreatedImage> adapter;

    private ProgressDialog mDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
        client = new DbxClientV2(config, ACCESS_TOKEN);

        treatedImages = new ArrayList<>();

        activity = this;
        setContentView(R.layout.activity_main);
        ListView listView = findViewById(R.id.listView);
        adapter = new ArrayAdapter<TreatedImage>(this, R.id.textView, treatedImages) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    view = inflater.inflate(R.layout.content_treated_images, parent, false);
                }
                ImageView imageView = view.findViewById(R.id.imageView);
                TextView textView = view.findViewById(R.id.textView);
                Uri uri = treatedImages.get(position).getImageUri();
                imageView.setImageURI(uri);
                String[] subs = uri.toString().split("/");
                String name = subs[subs.length - 1];
                textView.setText(name);
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(activity, TreatmentActivity.class);
                intent.putExtra("image", treatedImages.get(position));
                activity.startActivityForResult(intent, 3);
            }
        });
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Choisissez une imageRect");
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
                                        text.setText(R.string.image_camera_button);
                                        break;
                                    case 1:
                                        image.setImageDrawable(ContextCompat.getDrawable(parent.getContext(), android.R.drawable.ic_menu_gallery));
                                        text.setText(R.string.image_gallery_button);
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
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GetTask gt = new GetTask();
                gt.execute();

                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        GetTask gt = new GetTask();
        gt.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 0 || requestCode == 1) {
                Intent intent = new Intent(this, TreatmentActivity.class);
                if (requestCode == 1) {
                    imageUri = data.getData();
                }
                TreatedImage treatedImage = new TreatedImage(imageUri, new HashMap<RectF, String>());
                intent.putExtra("image", treatedImage);
                startActivityForResult(intent, 2);
            }
        }
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putParcelableArrayList("treatedImages", treatedImages);
//        outState.putInt("current", current);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        treatedImages = savedInstanceState.getParcelableArrayList("treatedImages");
//        current = savedInstanceState.getInt("current");
//    }

    protected void sendRequest(int which) {
        switch (which) {
            case 0:
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePicture.resolveActivity(this.getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }
                    if (photoFile != null) {
                        imageUri = FileProvider.getUriForFile(this,"com.android.test.fileprovider", photoFile);
                        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(takePicture, 0);
                    }
                }
                break;
            case 1:
                Intent pickPhoto = new Intent(Intent.ACTION_OPEN_DOCUMENT,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto, 1);
                break;
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    class GetTask extends AsyncTask<Void, String, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            treatedImages.clear();
            mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage("Getting informations from Django...");
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            mDialog.cancel();
            mDialog = new ProgressDialog(MainActivity.this);
            mDialog.setMessage("Downloading file " + values[0] + "...");
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mDialog.cancel();
            adapter.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {
                URL url = new URL("http://" + IP_ADDRESS + ":8000/treated_image/");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("GET");
                BufferedReader br = new BufferedReader(new InputStreamReader((con.getInputStream())));
                StringBuilder sb = new StringBuilder();
                String output;
                while ((output = br.readLine()) != null) {
                    sb.append(output);
                }
                String result = sb.toString();
                int response = con.getResponseCode();
                if (response == 200) {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray results = (JSONArray)jsonObject.get("results");
                    for (int i = 0 ; i < results.length() ; i++) {
                        JSONObject image = (JSONObject)results.get(i);
                        String filePath = (String)image.get("path");
                        String id_url = (String)image.get("url");
                        int id = id_url.charAt(id_url.length() - 2) - '0';
                        publishProgress(filePath);
                        JSONArray rects = (JSONArray)image.get("rects");
                        Map<RectF, String> descriptions = new HashMap<>();
                        for (int j = 0 ; j < rects.length() ; j++) {
                            JSONObject rect = (JSONObject)rects.get(j);
                            RectF rectF = new RectF();
                            rectF.top = ((Double)rect.get("top")).floatValue();
                            rectF.left = ((Double)rect.get("left")).floatValue();
                            rectF.bottom = ((Double)rect.get("bottom")).floatValue();
                            rectF.right = ((Double)rect.get("right")).floatValue();
                            String desc = (String)rect.get("description");
                            descriptions.put(rectF, desc);
                        }
                        File file = new File(MainActivity.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), filePath.substring(1));
                        System.out.println(file.getAbsolutePath());
                        if (!file.exists()) {
                            FileOutputStream fos = new FileOutputStream(file);
                            client.files().downloadBuilder(filePath).download(fos);
                        }
                        Uri uri = FileProvider.getUriForFile(MainActivity.this,"com.android.test.fileprovider", file);
                        TreatedImage treatedImage = new TreatedImage(uri, descriptions);
                        treatedImage.setId(id);
                        treatedImages.add(treatedImage);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
