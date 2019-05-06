package com.example.agi.navdrawerandimagepickertestapp.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
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
import com.example.agi.navdrawerandimagepickertestapp.azureCognitiveService.AsyncResponse;
import com.example.agi.navdrawerandimagepickertestapp.azureCognitiveService.OcrRequest;
import com.example.agi.navdrawerandimagepickertestapp.imageUtil.imageUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class FragmentHome extends Fragment {

    private static final String TAG = "FragmentHome";

    private final int PICK_IMAGE = 1;

    /*
    value changed in createImageFile()
    used in getFitPic(), getFullPic()
    used as param by autoRotateBitmap()
     */
    String currentPhotoPath;

    /*
    value changed in createImageFile()
    used in deleteImageFile()
     */
    String currentPhotoName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        Button btnCamImg = view.findViewById(R.id.btnCam);
        Button btnSelImg = view.findViewById(R.id.btnPick);
        Button btnUrlImg = view.findViewById(R.id.btnUrl);

        btnCamImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // simpleTakePicture();
                dispatchTakePictureIntent();
            }
        });

        btnSelImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Image"),
                        PICK_IMAGE);
            }
        });

        btnUrlImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(getContext());
                @SuppressLint("InflateParams")
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
                                        String imUrl = userInput.getText().toString();

                                        Toast toast = Toast.makeText(getContext(),
                                                "Retrieving image data...",
                                                Toast.LENGTH_SHORT);
                                        toast.show();
                                        new ImageFromUrl(FragmentHome.this).execute(imUrl);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK &&
                data != null && data.getData() != null){
            Uri uri = data.getData();
            Log.d(TAG, "PATH URI: " + uri.getPath());
            try {
                Bitmap bitmap = MediaStore.Images.Media.
                        getBitmap(Objects.requireNonNull(getContext()).getContentResolver(), uri);
                bitmap = imageUtil.autoRotateBitmap(getContext(), uri, bitmap);
                ImageView imageView = Objects.requireNonNull(getActivity()).findViewById(R.id.ivImg);
                imageView.setImageBitmap(bitmap);

//                String ocr_texts = ocr.executeOcrRequest(getContext(),
//                        imgOutputStream(bitmap));
                final TextView textView = getActivity().findViewById(R.id.tvTest);
//                textView.setText(ocr_texts);
                OcrRequest ocrRequest = new OcrRequest(new AsyncResponse() {
                    @Override
                    public void processFinish(Object output) {
                        textView.setText((String) output);
                    }
                }, imgOutputStream(bitmap), getContext());
                ocrRequest.execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (requestCode == REQUEST_TAKE_PHOTO &&
                resultCode == Activity.RESULT_OK) {
            Bitmap image = imageUtil.autoRotateBitmap(
                    currentPhotoPath, imageUtil.getFullPic(currentPhotoPath));
            ImageView imageView = Objects.requireNonNull(getActivity()).findViewById(R.id.ivImg);
            imageView.setImageBitmap(image);

//            String ocr_texts = ocr.executeOcrRequest(
//                    getContext(),
//                    imgOutputStream(image)
//            );
            final TextView textView = getActivity().findViewById(R.id.tvTest);
//            textView.setText(ocr_texts);
            OcrRequest ocrRequest = new OcrRequest(new AsyncResponse() {
                @Override
                public void processFinish(Object output) {
                    textView.setText((String) output);
                }
            }, imgOutputStream(image), getContext());
            ocrRequest.execute();
            imageUtil.deleteImageFile(getActivity(), currentPhotoName);
        }
    }

    /**
     * Used to grab a bitmap from Url
     */
    private static class ImageFromUrl extends AsyncTask<String, Void, Bitmap>{
        private  WeakReference<FragmentHome> activityWeakReference;

        ImageFromUrl(FragmentHome context) {
            activityWeakReference = new WeakReference<>(context);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                return BitmapFactory.decodeStream((InputStream)new URL(strings[0]).getContent());
            } catch (IOException er) {
                er.printStackTrace();
                Log.d(TAG, "URLBITMAP!!!");
                return BitmapFactory.decodeResource(activityWeakReference.get().getResources(),
                        R.drawable.imgplaceholder);
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            ImageView imageView = Objects.requireNonNull(activityWeakReference.get().getActivity()).
                    findViewById(R.id.ivImg);
            imageView.setImageBitmap(bitmap);

//            String ocr_texts = ocr.executeOcrRequest(activityWeakReference.get().getContext(),
//                    activityWeakReference.get().imgOutputStream(bitmap));
            final TextView textView_ = Objects.requireNonNull(activityWeakReference.get().getActivity()).findViewById(R.id.tvTest);
//            textView_.setText(ocr_texts);
            OcrRequest ocrRequest = new OcrRequest(new AsyncResponse() {
                @Override
                public void processFinish(Object output) {
                    textView_.setText((String) output);
                }
            }, activityWeakReference.get().imgOutputStream(bitmap), activityWeakReference.get().getContext());
            ocrRequest.execute();
        }
    }

    /**
     * Method to convert image bitmap into ByteArrayOutputStream
     * @param bitmap The image bitmap to be converted
     * @return The ByteArrayOutputStream from the bitmap
     */
    public ByteArrayOutputStream imgOutputStream(Bitmap bitmap){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream;
    }

    /**
     * Method to create image file
     * @return The image File
     * @throws IOException Errors while creating the file
     */
    private File createImageFile() throws IOException {
        // create an image file name
        String timeStamp = new SimpleDateFormat(
                "yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Objects.requireNonNull(getActivity()).
                getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName, //prefix
                ".jpg", //suffix
                storageDir //directory
        );

        currentPhotoPath = image.getAbsolutePath();
        currentPhotoName = imageFileName + ".jpg";
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 3;
    /**
     * Method to take full resolution photo using Intent MediaStore.ACTION_IMAGE_CAPTURE
     */
    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the request
        if (takePictureIntent.resolveActivity(Objects.requireNonNull(getActivity()).
                getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            if (photoFile != null) {
                Log.d(TAG, "dispatchTakePictureIntent: START__");
                Uri photoURI = FileProvider.getUriForFile(Objects.requireNonNull(getContext()),
                        "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }
}
