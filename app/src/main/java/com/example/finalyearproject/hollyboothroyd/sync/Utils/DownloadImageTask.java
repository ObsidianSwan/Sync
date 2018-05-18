package com.example.finalyearproject.hollyboothroyd.sync.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageButton;

import java.io.InputStream;

/**
 * Created by hollyboothroyd
 * 4/16/2018.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    private ImageButton mImageButton;

    public DownloadImageTask(ImageButton bmImage) {
        this.mImageButton = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        Bitmap mIcon11 = null;
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        mImageButton.setImageBitmap(result);
    }
}

