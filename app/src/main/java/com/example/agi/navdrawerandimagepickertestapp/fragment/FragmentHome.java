package com.example.agi.navdrawerandimagepickertestapp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.agi.navdrawerandimagepickertestapp.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import java.net.URI;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class FragmentHome extends Fragment {

    private static final String TAG = "FragmentHome";

    private final int PICK_IMAGE = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;
    String imurl = null;
    Bitmap imBitmap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnCamImg = view.findViewById(R.id.btnCam);
        Button btnSelImg = view.findViewById(R.id.btnPick);
        Button btnUrlImg = view.findViewById(R.id.btnUrl);

        btnCamImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        btnSelImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE);
            }
        });

        btnUrlImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(getContext());
                View promptsView = li.inflate(R.layout.prompt_imurl, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

                alertDialogBuilder.setView(promptsView);

                final EditText userInput = promptsView.findViewById(R.id.editTextDialogUserInput);

                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        imurl = userInput.getText().toString();

                                        Toast toast = Toast.makeText(getContext(), "Retrieving image data...", Toast.LENGTH_SHORT);
                                        toast.show();
                                        new ImageFromUrl(FragmentHome.this).execute(imurl);
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.show();
            }
        });

        return view;
    }

    private class ImageFromUrl extends AsyncTask<String, Void, Bitmap>{
        private  WeakReference<FragmentHome> activityWeakReference;

        public ImageFromUrl(FragmentHome context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream((InputStream)new URL(strings[0]).getContent());
                return bitmap;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return BitmapFactory.decodeResource(getResources(), R.drawable.imgplaceholder);
            } catch (IOException e) {
                e.printStackTrace();
                return BitmapFactory.decodeResource(getResources(), R.drawable.imgplaceholder);
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView imageView = getActivity().findViewById(R.id.ivImg);
            imageView.setImageBitmap(bitmap);
            imBitmap = bitmap;
            new ocr_request(FragmentHome.this).execute(imgOutputStream(imBitmap));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null){
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                ImageView imageView = getActivity().findViewById(R.id.ivImg);
                imageView.setImageBitmap(bitmap);
                imBitmap = bitmap;
                new ocr_request(FragmentHome.this).execute(imgOutputStream(imBitmap));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView imageView = getActivity().findViewById(R.id.ivImg);
            imageView.setImageBitmap(imageBitmap);
            imBitmap = imageBitmap;
            new ocr_request(FragmentHome.this).execute(imgOutputStream(imBitmap));
        }
    }

    public ByteArrayOutputStream imgOutputStream(Bitmap bitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream;
    }

    public static String post_request_ocr(ByteArrayOutputStream outputStream){

        HttpClient httpclient = HttpClients.createDefault();

        try
        {
            Log.d(TAG, "post_request_ocr: initializing URIBuilder...");
            URIBuilder builder = new URIBuilder("https://southeastasia.api.cognitive.microsoft.com/vision/v2.0/ocr");

            Log.d(TAG, "post_request_ocr: setting builder parameter...");
            builder.setParameter("language", "unk");
            builder.setParameter("detectOrientation", "true");

            Log.d(TAG, "post_request_ocr: building...");
            URI uri = builder.build();

            Log.d(TAG, "post_request_ocr: setting request header...");
            HttpPost request = new HttpPost(uri);
            request.setHeader("Content-Type", "application/octet-stream");
            request.setHeader("Ocp-Apim-Subscription-Key", "f21b4f194bb1480c8dde294d9baf18e7");

            Log.d(TAG, "post_request_ocr: setting request body...");
            // Request body
            ByteArrayEntity reqEntity = new ByteArrayEntity(outputStream.toByteArray());
            //StringEntity reqEntity = new StringEntity("{\"url\":\"http://downloads.bbc.co.uk/skillswise/english/en03text/images/en03text-v-types-of-text-448x252.jpg\"}");
            request.setEntity(reqEntity);
            //Log.d(TAG, "request body: " + "{\"url\":\"http://downloads.bbc.co.uk/skillswise/english/en03text/images/en03text-v-types-of-text-448x252.jpg\"}");

            Log.d(TAG, "post_request_ocr: executing request...");
            HttpResponse response = httpclient.execute(request);

            Log.d(TAG, "post_request_ocr: receiving response...");
            HttpEntity entity = response.getEntity();

            if (entity != null)
            {
                StringBuilder sb = new StringBuilder();
                String the_response = EntityUtils.toString(entity);
                Log.d(TAG, "post_request_ocr_1: " + the_response);

                Log.d(TAG, "post_request_ocr: reading json...");
                JSONObject json = (JSONObject) new JSONTokener(the_response).nextValue();
                Log.d(TAG, "post_request_ocr: reading regions array");
                JSONArray json2 = json.getJSONArray("regions");
                for (int i = 0; i < json2.length(); i++) {
                    Log.d(TAG, "post_request_ocr: opening json array in regions");
                    JSONObject json3 = json2.getJSONObject(i);
                    Log.d(TAG, "post_request_ocr: reading lines array");
                    JSONArray json4 = json3.getJSONArray("lines");
                    for (int j = 0; j < json4.length(); j++) {
                        Log.d(TAG, "post_request_ocr: opening json array in lines");
                        JSONObject json5 = json4.getJSONObject(j);
                        Log.d(TAG, "post_request_ocr: reading words array");
                        JSONArray json6 = json5.getJSONArray("words");
                        for (int k = 0; k < json6.length(); k++) {
                            Log.d(TAG, "post_request_ocr: opening json array in words");
                            JSONObject json7 = json6.getJSONObject(k);
                            String test = (String) json7.get("text");
                            sb.append(test).append(" ");
                        }
                        sb.append("\n");
                    }
                    sb.append("\n\n");
                }

                Log.d(TAG, "request_ocr LINES: " + sb);
                return sb.toString();
            }
            return "NULL";
        }
        catch (Exception e)
        {
            //System.out.println(e.getMessage());
            e.printStackTrace();
            Log.d(TAG, "post_request_ocr_2: " + e.getMessage());
            return e.getMessage();
        }

    }

    private static class ocr_request extends AsyncTask<ByteArrayOutputStream, String, String>{
        private WeakReference<FragmentHome> activityWeakReference;

        public ocr_request(FragmentHome context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected String doInBackground(ByteArrayOutputStream... outputStream) {
            return post_request_ocr(outputStream[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            Activity activity = activityWeakReference.get().getActivity();
            if (activity != null) {
                TextView textView = activity.findViewById(R.id.tvTest);
                textView.setText(s);
            }
        }
    }

}
