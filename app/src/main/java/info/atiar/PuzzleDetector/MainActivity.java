package info.atiar.PuzzleDetector;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private FirebaseAnalytics mFirebaseAnalytics;
    ImageView imageView1, imageView2;

    boolean i1 = false;
    boolean i2 = false;

    Mat img , templ;

    //Loading opencv on base loader.
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    //img=readImageFromResources();

                    //templ=readImageFromResources1();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OpenCVLoader.initDebug();
        setContentView(R.layout.activity_main1);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        imageView1 =  findViewById(R.id.input1);
        imageView2 =  findViewById(R.id.input2);
    }

    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

          /*  Intent intent = new Intent(MainActivity.this, MyService.class);
            startService(intent);*/

        }
    }

    //UI Onlclick listner
    public void input1Onclick(View view) {
        i1=true;
        i2=false;
        opengalary();
    }

    public void input2Onclick(View view) {
        i1=false;
        i2=true;
        opengalary();
    }

    public void findCurrectLocation(View view) {


        if (img==null){
            BP.showDialog(this,"Please insert valid raw image.");
            return;
        }

        if (templ==null){
            BP.showDialog(this,"Please insert valid template image.");
            return;
        }

        if (img.height()<=0 || img.width() <=0){
            BP.showDialog(this,"Please insert valid raw image.");
            return;
        }
        if (templ.height()<=0 || templ.width() <=0){
            BP.showDialog(this,"Please insert valid template image.");
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new MatchingDemo().run(Imgproc.TM_CCOEFF);
            }
        });

    }

    public void reset(View view) {
        Intent intent =  new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }


    //Image picker with croper feature.
    public void opengalary() {
       CropImage.activity(null).setGuidelines(CropImageView.Guidelines.ON).start(this);
    }

    //waiting for the result of croped image and sent the cropped image to imageview and mat for template matching.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                if (i1){
                    i1=false;
                    i2=true;
                    imageView1.setImageBitmap(convetUriToBitmap(resultUri,1));
                    img = new Mat();
                    Utils.bitmapToMat(convetUriToBitmap(resultUri,1),img,true);
                }else {
                    i1=true;
                    i2=false;
                    imageView2.setImageBitmap(convetUriToBitmap(resultUri,2));
                    templ = new Mat();
                    Utils.bitmapToMat(convetUriToBitmap(resultUri,2),templ,true);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }



    public Bitmap convetUriToBitmap(Uri uri, int inpurFrom){
        Bitmap convertedBitmap = null;

        try {
            if (inpurFrom==1){
                convertedBitmap = decodeSampledBitmapFromUri(uri,500);
            }else {
                convertedBitmap = decodeSampledBitmapFromUri(uri,80);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return convertedBitmap;
    }

    //OpenCV class is responsible for Template Matching
    public class MatchingDemo {

        public void run(int match_method) {
            System.out.println("\nRunning Template Matching");
            Log.e("atiar - ", "img height = " + img.height() + " img width = " + img.width());
            Log.e("atiar - ", "templ height = " + templ.height() + " temol width = " + templ.width());


            // / Create the result matrix
            int result_cols = img.cols() - templ.cols() + 1;
            int result_rows = img.rows() - templ.rows() + 1;
            Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);

            Log.e("atiar - ", "img height = " + img.height());
            // / Do the Matching and Normalize
            Imgproc.matchTemplate(img, templ, result, match_method);
            Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

            // / Localizing the best match with minMaxLoc
            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

            Point matchLoc;
            if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
                matchLoc = mmr.minLoc;
            } else {
                matchLoc = mmr.maxLoc;
            }

            // / Show me what you got
            Imgproc.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(),
                    matchLoc.y + templ.rows()), new Scalar(0, 0, 255),4);

            //print the output file.
            showImg(img);

        }
    }

    private void showImg(Mat img) {
        final Bitmap bm = Bitmap.createBitmap(img.cols(), img.rows(),Bitmap.Config.RGB_565);
        Utils.matToBitmap(img, bm);
        final ImageView imageView = (ImageView) findViewById(R.id.input1);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                imageView.setImageBitmap(bm);
            }
        });
    }

    public Bitmap decodeSampledBitmapFromUri(Uri uri, int minReqSize) throws FileNotFoundException {

        BitmapFactory.Options options = new BitmapFactory.Options();
        // First decode with inJustDecodeBounds=true to check dimensions
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver()
                .openInputStream(uri), null, options);
        // The new size we want to scale to
        final int REQUIRED_SIZE = minReqSize;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = options.outWidth, height_tmp = options.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }
        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;

        return BitmapFactory.decodeStream(getContentResolver()
                .openInputStream(uri), null, o2);
    }

}
