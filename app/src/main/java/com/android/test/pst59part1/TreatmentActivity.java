package com.android.test.pst59part1;

import android.content.Intent;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

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
                PostTask pt = new PostTask();
                pt.setTreatedImage(treatedImage);
                pt.execute();
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

    public void sendData(TreatedImage treatedImage) throws IOException, JSONException {
        URL url;
        HttpURLConnection urlConn;
        DataOutputStream printout;
        DataInputStream input;
        url = new URL("http://192.168.1.13:8000/treated_image/");
        urlConn = (HttpURLConnection) url.openConnection();
//        urlConn.setDoInput(true);
//        urlConn.setDoOutput(true);
//        urlConn.setUseCaches(false);
        urlConn.setRequestProperty("Content-Type","application/json");
        urlConn.setRequestProperty("Host", "http://192.168.1.13");
        urlConn.connect();
        //Create JSONObject here
        JSONObject[] rects = new JSONObject[treatedImage.getDescription().keySet().size()];
        int cpt = 0;
        for(RectF description : treatedImage.getDescription().keySet()) {
            JSONObject rect = new JSONObject();
            rect.put("top", description.top);
            rect.put("left", description.left);
            rect.put("bottom", description.bottom);
            rect.put("right", description.right);
            rect.put("description", treatedImage.getDescription().get(description));
            rects[cpt] = rect;
            cpt++;
        }
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("rects", rects);
        jsonParam.put("image", "null");
        printout = new DataOutputStream(urlConn.getOutputStream());
        printout.writeBytes(URLEncoder.encode(jsonParam.toString(),"UTF-8"));
        printout.flush();
        printout.close();
    }

    class PostTask extends AsyncTask<Void, Void, Void> {

        TreatedImage treatedImage;

        public void setTreatedImage(TreatedImage treatedImage) {
            this.treatedImage = treatedImage;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL("http://192.168.1.13:8000/treated_image/");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setRequestProperty("Accept", "application/json");
                con.setRequestMethod("POST");
                String rects = "[";
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
                        rects += rect.toString() + ", ";
                    } else {
                        rects += rect.toString() + "]";
                    }
                }
                JSONObject jsonParam = new JSONObject();
                JSONArray jsonRects = new JSONArray(rects);
                jsonParam.put("rects", jsonRects);
                jsonParam.put("image", "http://www.google.com/");
                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                System.out.println(jsonParam.toString());
                wr.write(jsonParam.toString());
                wr.flush();
                String responseMsg = con.getResponseMessage();
                int response = con.getResponseCode();
                System.out.println(responseMsg + ' ' + response);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
