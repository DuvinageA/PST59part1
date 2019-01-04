package com.android.test.pst59part1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TreatmentActivity extends AppCompatActivity implements SelectableImageView.SelectableImageViewListener {

    final static private String ACCESS_TOKEN = "K4DN47ESdsAAAAAAAAAB6MtUjfCbTVcNRBLalSWyoKcX9HVUgUx04lWcOjOt7Bob";
    private static final String IP_ADDRESS = "192.168.1.13";

    protected TreatedImage treatedImage;
    protected SelectableImageView selectableImageView;
    protected FloatingActionButton temporaryFab;
    protected Menu menu;
    protected int imageHeight;
    protected int imageWidth;

    private DbxClientV2 client;

    private ProgressDialog mDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_treatment);
        treatedImage = getIntent().getParcelableExtra("image");

        DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build();
        client = new DbxClientV2(config, ACCESS_TOKEN);

        selectableImageView = findViewById(R.id.image);
        Uri uri = treatedImage.getImageUri();
        String string = uri.toString();
        selectableImageView.setImageURI(Uri.parse(string));
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
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "_";
                File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageFileName + ".jpg");
                try {
                    InputStream in = getContentResolver().openInputStream(treatedImage.getImageUri());
                    OutputStream out = new FileOutputStream(file);
                    byte[] buf = new byte[1024];
                    int len;
                    if (in != null) {
                        while((len=in.read(buf))>0){
                            out.write(buf,0,len);
                        }
                        out.close();
                        in.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                PostTask pt = new PostTask();
                pt.setFile(file);
                pt.setTreatedImage(treatedImage);
                pt.execute();


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

    class PostTask extends AsyncTask<Void, Void, String> {

        File file;

        public void setFile(File file) { this.file = file; }

        TreatedImage treatedImage;

        void setTreatedImage(TreatedImage treatedImage) {
            this.treatedImage = treatedImage;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(TreatmentActivity.this);
            mDialog.setMessage("Uploading " + file.getName() + "...");
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            if (result == null)
                System.err.println("An error has occured.");
            else
                System.out.println(result);

            mDialog.cancel();

            System.out.println("Post task successfully executed!");

            Intent returnIntent = new Intent();
            returnIntent.putExtra("image", treatedImage);
            setResult(RESULT_OK, returnIntent);

            System.out.println("Back to main activity...");

            finish();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String fileURL = "http://www.android.com";
                String filePath = "/android";
                if (treatedImage.getId() == -1) {
                    FileInputStream inputStream = new FileInputStream(file);
                    FileMetadata metadata = client.files().uploadBuilder("/" + file.getName()).uploadAndFinish(inputStream);
                    SharedLinkMetadata sharedLinkMetadata = client.sharing().createSharedLinkWithSettings(metadata.getPathLower());
                    filePath = metadata.getPathLower();
                    fileURL = sharedLinkMetadata.getUrl();
                }
                URL url;
                String requestMethod;
                if (treatedImage.getId() == -1) {
                    url = new URL("http://" + IP_ADDRESS + ":8000/treated_image/");
                    requestMethod = "POST";
                } else {
                    url = new URL("http://" + IP_ADDRESS + ":8000/treated_image/" + treatedImage.getId() + '/');
                    requestMethod = "PUT";
                }
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod(requestMethod);
                StringBuilder rects = new StringBuilder("[");
                int cpt = 0;
                int size = treatedImage.getDescription().keySet().size();
                for(RectF description : treatedImage.getDescription().keySet()) {
                    JSONObject rect = new JSONObject();
                    rect.put("top", description.top);
                    rect.put("left", description.left);
                    rect.put("bottom", description.bottom);
                    rect.put("right", description.right);
                    rect.put("description", treatedImage.getDescription().get(description));
                    cpt++;
                    if (cpt < size) {
                        rects.append(rect.toString()).append(", ");
                    } else {
                        rects.append(rect.toString()).append("]");
                    }
                }
                JSONObject jsonParam = new JSONObject();
                JSONArray jsonRects = new JSONArray(rects.toString());
                jsonParam.put("rects", jsonRects);
                jsonParam.put("link", fileURL);
                jsonParam.put("path", filePath);
                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                wr.write(jsonParam.toString());
                wr.flush();
                String responseMsg = con.getResponseMessage();
                int response = con.getResponseCode();
                return responseMsg + ' ' + response;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
