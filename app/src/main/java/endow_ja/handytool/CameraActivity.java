package endow_ja.handytool;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import endow_ja.handytool.Helper.CameraPreview;

public class CameraActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;

    static private String TAG = "tem ";
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            Log.e(TAG, "getCameraInstance: no activity_camera instance" );
        }
        return c; // returns null if activity_camera is unavailable
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Create an instance of Camera
        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        // Create our Preview view and set it as the content of our activity.
        Camera.PictureCallback callback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {

                Bitmap realImage = BitmapFactory.decodeByteArray(data , 0, data .length);

                if(realImage!=null){

                    try
                    {
                        File file = createImage();

                        ExifInterface exif=new ExifInterface(file.toString());

                        Log.d("EXIF value", exif.getAttribute(ExifInterface.TAG_ORIENTATION));
                        if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("6")){
                            realImage= rotate(realImage, 90);
                        } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("8")){
                            realImage= rotate(realImage, 270);
                        } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("3")){
                            realImage= rotate(realImage, 180);
                        } else if(exif.getAttribute(ExifInterface.TAG_ORIENTATION).equalsIgnoreCase("0")){
                            realImage= rotate(realImage, 90);
                        }

                        FileOutputStream fileOutputStream=new FileOutputStream(file);
                        realImage.compress(Bitmap.CompressFormat.JPEG,100, fileOutputStream);
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }
                    catch(IOException e){
                        e.printStackTrace();
                    }
                    catch(Exception exception)
                    {
                        exception.printStackTrace();
                    }
                    if (mCamera != null){
                        mCamera.release();        // release the activity_camera for other applications
                        mCamera = null;
                    }
                    Toast.makeText(getApplicationContext(), "foto scattata", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }};
        mPreview = new CameraPreview(this, mCamera, callback);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

        //take photo
        preview.addView(mPreview);
    }

    private File createImage() throws IOException {
        // Create an image file name
        Log.d("handytool", "createimage: begin");
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "HandyTool");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(image);
        mediaScanIntent.setData(contentUri);
        getApplicationContext().sendBroadcast(mediaScanIntent);

        return image;
    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        //       mtx.postRotate(degree);
        mtx.setRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

}

