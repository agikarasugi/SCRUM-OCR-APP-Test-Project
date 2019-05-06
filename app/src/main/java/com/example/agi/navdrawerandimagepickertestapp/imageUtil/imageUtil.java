package com.example.agi.navdrawerandimagepickertestapp.imageUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.support.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class imageUtil {
    private static final String TAG = "imageUtil";

    /**
     * Method to rotate image Bitmap
     * @param source The image Bitmap to be rotated
     * @param angle The angle for the rotation
     * @return The rotated image Bitmap
     */
    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private static Bitmap getNormalOrientation(ExifInterface ei, Bitmap photo) {
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Bitmap rotatedBitmap;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotatedBitmap = rotateImage(photo, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                rotatedBitmap = rotateImage(photo, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                rotatedBitmap = rotateImage(photo, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                rotatedBitmap = photo;
        }
        return rotatedBitmap;
    }

    /**
     * Method to detect image orientation from EXIF
     * and rotate it according the EXIF data
     *
     * @param photoPath The path of the image data, uses
     * @link currentPhotoPath
     *
     * @param photo The Bitmap of the image
     * @return The rotated image Bitmap
     */
    public static Bitmap autoRotateBitmap(String photoPath, Bitmap photo) {
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(photoPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (ei != null) {
            return getNormalOrientation(ei, photo);

        } else {
            Log.d(TAG, "autoRotateBitmap: FAIL");
        }
        return photo;
    }

    /**
     * Method to detect image orientation from EXIF
     * and rotate it according to the EXIF data
     *
     * @param context The context of the activity
     * @param uri The Uri of the image file
     * @param photo The Bitmap of the image
     * @return The rotated image Bitmap
     */
    public static Bitmap autoRotateBitmap(Context context, Uri uri, Bitmap photo){
        InputStream in = null;
        try {
            in = context.getContentResolver().openInputStream(uri);
            if (in != null) {
                ExifInterface exifInterface = new ExifInterface(in);
                photo = getNormalOrientation(exifInterface, photo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return photo;
    }

//    /**
//     * Method to get image Bitmap from currentPhotoPath with scaling for ImageView
//     * @param photoPath The path of the image file
//     * @param targetWidth The width of the target ImageView
//     * @param targetHeight The height of the target ImageView
//     * @return The scaled Bitmap
//     */
//    @SuppressWarnings("deprecation")
//    public static Bitmap getFitPic(String photoPath, int targetWidth, int targetHeight) {
//        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//        bmOptions.inJustDecodeBounds = true;
//        BitmapFactory.decodeFile(photoPath, bmOptions);
//        int photoW = bmOptions.outWidth;
//        int photoH = bmOptions.outHeight;
//
//        int scaleFactor = Math.min(photoW/targetWidth, photoH/targetHeight);
//
//        bmOptions.inJustDecodeBounds = false;
//        bmOptions.inSampleSize = scaleFactor;
//        bmOptions.inPurgeable = true;
//
//        return BitmapFactory.decodeFile(photoPath, bmOptions);
//    }

    /**
     * Method to get image Bitmap from currentPhotoPath
     * @param photoPath The path to the image file
     * @return The image Bitmap
     */
    @SuppressWarnings("deprecation")
    public static Bitmap getFullPic(String photoPath) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inPurgeable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);

        return Bitmap.createScaledBitmap(bitmap, 1520, 2592, false);
    }

    /**
     * Method to delete image file
     */
    public static void deleteImageFile(Context context, String photoFileName) {
        File file = new File(Objects.requireNonNull(context).getExternalFilesDir(Environment.DIRECTORY_PICTURES), photoFileName);
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }
}
