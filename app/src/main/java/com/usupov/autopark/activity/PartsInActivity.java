package com.usupov.autopark.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.usupov.autopark.R;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PartsInActivity extends AppCompatActivity {

    public static final int TAKE_PICTURE_REQUEST_B = 200;
    private Bitmap mCameraBitmap;
    LinearLayout linLayoutPhotoParts;
    LayoutInflater inflater;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parts_in);
        String partInName = "Рычаг подвески, передний левый";
        TextView tViewPartInname = (TextView) findViewById(R.id.part_in_name);
        tViewPartInname.setText(partInName);

        findViewById(R.id.btn_photo_part_in).setOnClickListener(mCaptureImageButtonClickListener);
        linLayoutPhotoParts = (LinearLayout)findViewById(R.id.layout_parts_in);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

    }
    private View.OnClickListener mCaptureImageButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startImageCapture();
        }
    };
    private void startImageCapture() {
        startActivityForResult(new Intent(PartsInActivity.this, CameraActivity.class), TAKE_PICTURE_REQUEST_B);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TAKE_PICTURE_REQUEST_B) {
            if (resultCode == RESULT_OK) {

                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                // Recycle the previous bitmap.
                if (mCameraBitmap != null) {
                    mCameraBitmap.recycle();
                    mCameraBitmap = null;
                }
                Bundle extras = data.getExtras();

                String imagePath = extras.getString(CameraActivity.EXTRA_CAMERA_DATA);
                if (imagePath != null && !imagePath.equals("")) {
                    RelativeLayout layout = (RelativeLayout) findViewById(R.id.photoCarField);
//                    layout.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;

//                    layout.setVisibility(View.VISIBLE);

                    // Get the dimensions of the bitmap
//                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//                    bmOptions.inJustDecodeBounds = true;
//                    BitmapFactory.decodeFile(imagePath, bmOptions);

                    int targetH = dpToPx(200);//mCameraImageView.getWidth();
                    int targetW = dpToPx(1);



//                    int photoW = bmOptions.outWidth;
//                    int photoH = bmOptions.outHeight;

//                    if (photoH > photoW) {
//                        targetW = 1;//mCameraImageView.getWidth();
//                        targetH = 300;
//                    }
//                    Toast.makeText(PartsInActivity.this, photoH+" "+photoW, Toast.LENGTH_LONG).show();
                    // Determine how much to scale down the image
//                    int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

                    // Decode the image file into a Bitmap sized to fill the View
//                    bmOptions.inJustDecodeBounds = false;
//                    bmOptions.inSampleSize = scaleFactor;
//                    bmOptions.inPurgeable = true;

//                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
                    Bitmap bitmap = resizeBitmap(imagePath, targetW, targetH);

//                    ImageView mCameraImageView = (ImageView)findViewById(R.id.image_part_in);

//                    mCameraImageView.setImageBitmap(bitmap);

//                    ImageView closeCarPhoto = (ImageView) findViewById(R.id.closeCarPhoto);
//                    closeCarPhoto.setVisibility(View.VISIBLE);

//                    mCameraImageView.setVisibility(View.VISIBLE);

                    inflater = getLayoutInflater();
                    View viev = inflater.inflate(R.layout.item_part_in, null, false);
                    final ImageView image = (ImageView) viev.findViewById(R.id.image_part_in);
                    final ImageView imageClose = (ImageView)viev.findViewById(R.id.image_part_in_close);
                    image.setImageBitmap(bitmap);
                    imageClose.setImageResource(R.drawable.ic_action_close);

                    imageClose.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            image.setVisibility(View.GONE);
                            imageClose.setVisibility(View.GONE);
                        }
                    });
                    linLayoutPhotoParts.addView(viev);
                    Toast.makeText(PartsInActivity.this, bitmap.getHeight()+" "+bitmap.getWidth(), Toast.LENGTH_LONG).show();
                }
            } else {
                mCameraBitmap = null;
            }
        }
    }
    private int dpToPx(int dp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }
    public Bitmap resizeBitmap(String photoPath, int targetW, int targetH) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true; //Deprecated API 21

        return BitmapFactory.decodeFile(photoPath, bmOptions);
    }
}