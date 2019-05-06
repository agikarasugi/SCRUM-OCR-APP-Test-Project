package com.example.agi.navdrawerandimagepickertestapp.azureCognitiveService;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OcrRequest extends AsyncTask<Void, String, String> {
    private ByteArrayOutputStream outputStream;
    private AsyncResponse delegate;
    private ProgressDialog progressDialog;

    public OcrRequest(AsyncResponse asyncResponse, ByteArrayOutputStream outputStream, Context context) {
        delegate = asyncResponse;
        this.outputStream = outputStream;
        progressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
        progressDialog.setMessage("Please wait");
    }

    @Override
    protected String doInBackground(Void... voids) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://southeastasia.api.cognitive.microsoft.com/vision/v2.0/ocr").newBuilder();
        urlBuilder.addQueryParameter("language", "unk");
        urlBuilder.addQueryParameter("detectOrientation", "true");

        String url = urlBuilder.build().toString();

        RequestBody body = RequestBody.create(
                MediaType.parse("application/octet-stream; charset=utf-8"), outputStream.toByteArray());

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("Ocp-Apim-Subscription-Key", "f21b4f194bb1480c8dde294d9baf18e7")
                .post(body)
                .build();

        OkHttpClient client = new OkHttpClient.Builder().build();

        Call call = client.newCall(request);
        Response response;
        try {
            response = call.execute();
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }

        String responseString;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            responseString = response.body().string();

            JSONObject json = (JSONObject) new JSONTokener(responseString).nextValue();
            JSONArray json2 = json.getJSONArray("regions");
            for (int i = 0; i < json2.length(); i++) {
                JSONObject json3 = json2.getJSONObject(i);
                JSONArray json4 = json3.getJSONArray("lines");
                for (int j = 0; j < json4.length(); j++) {
                    JSONObject json5 = json4.getJSONObject(j);
                    JSONArray json6 = json5.getJSONArray("words");
                    for (int k = 0; k < json6.length(); k++) {
                        JSONObject json7 = json6.getJSONObject(k);
                        String test = (String) json7.get("text");
                        stringBuilder.append(test).append(" ");
                    }
                    stringBuilder.append("\n");
                }
                stringBuilder.append("\n\n");
            }

            responseString =  stringBuilder.toString();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            responseString = e.getMessage();
        }

        return responseString;
    }

    @Override
    protected void onPostExecute(String response) {
        progressDialog.dismiss();
        delegate.processFinish(response);
    }
}
