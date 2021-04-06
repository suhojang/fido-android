package com.kcert.fido.face.ui.camera;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.google.android.gms.vision.CameraSource;
import com.kcert.fido.FaceActivity;
import com.kcert.fido.parent.FIDOProperties;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.View;

public class GraphicOverlay extends View {
    private final Object mLock = new Object();
    private int mPreviewWidth;
    private float mWidthScaleFactor = 1.0f;
    private int mPreviewHeight;
    private float mHeightScaleFactor = 1.0f;
    private int mFacing = CameraSource.CAMERA_FACING_BACK;
    private Set<Graphic> mGraphics = new HashSet<>();

    private Context mContext;
    private CameraSource mCameraSource;
    private boolean mMode;

    public static abstract class Graphic {
        private GraphicOverlay mOverlay;

        public Graphic(GraphicOverlay overlay) {
            mOverlay = overlay;
        }

        public abstract void draw(Canvas canvas);

        /**
         * 미리보기 x좌표 조정
         */
        public float translateX(float x) {
            if (mOverlay.mFacing == CameraSource.CAMERA_FACING_FRONT) {
                return mOverlay.getWidth() - scaleX(x);
            } else {
                return scaleX(x);
            }
        }

        /**
         * 미리보기 y좌표 조정
         */
        public float translateY(float y) {
            return scaleY(y);
        }

        /**
         * 가로값을 미리보기 배율로 조정
         */
        public float scaleX(float horizontal) {
            return horizontal * mOverlay.mWidthScaleFactor;
        }

        /**
         * 세로값을 미리보기 배율로 조정
         */
        public float scaleY(float vertical) {
            return vertical * mOverlay.mHeightScaleFactor;
        }

        public void postInvalidate() {
            mOverlay.postInvalidate();
        }

        public void capture() {
            mOverlay.capture();
        }
    }

    public GraphicOverlay(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 그래픽 클리어
     */
    public void clear() {
        synchronized (mLock) {
            mGraphics.clear();
        }
        postInvalidate();
    }

    /**
     * 그래픽 추가
     */
    public void add(Graphic graphic) {
        synchronized (mLock) {
            mGraphics.add(graphic);
        }
        postInvalidate();
    }

    /**
     * 그래픽 제거
     */
    public void remove(Graphic graphic) {
        synchronized (mLock) {
            mGraphics.remove(graphic);
        }
        postInvalidate();
    }

    public void setCameraInfo(int previewWidth, int previewHeight, int facing, CameraSource source, Context context, boolean mode) {
        synchronized (mLock) {
            mPreviewWidth = previewWidth;
            mPreviewHeight = previewHeight;
            mFacing = facing;
            mCameraSource   = source;
            mContext    = context;
            mMode   = mode;
        }
        postInvalidate();
    }

    /**
     * canvas에 그리기 작업(설정 된 그래픽 객체)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (mLock) {
            if ((mPreviewWidth != 0) && (mPreviewHeight != 0)) {
                mWidthScaleFactor = (float) canvas.getWidth() / (float) mPreviewWidth;
                mHeightScaleFactor = (float) canvas.getHeight() / (float) mPreviewHeight;
            }

            for (Graphic graphic : mGraphics) {
                graphic.draw(canvas);
            }
        }
    }

    public void capture(){
        mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes) {
                String tmp	= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + mContext.getPackageName() + File.separator + FIDOProperties.VERIFY_FACE_TMP_FILE_NAME;
                String path	= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + mContext.getPackageName() + File.separator + FIDOProperties.VERIFY_FACE_FILE_NAME;
                try {
                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    File tmpFile = null;
                    if (bmp != null) {
                        FileOutputStream fos = null;
                        try {
                            tmpFile = new File(tmp);
                            if (!tmpFile.getParentFile().exists())
                                tmpFile.getParentFile().mkdirs();
                            fos = new FileOutputStream(tmpFile);
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.flush();

                            BitmapFactory.Options bounds = new BitmapFactory.Options();
                            bounds.inJustDecodeBounds = true;
                            BitmapFactory.decodeFile(tmpFile.getAbsolutePath(), bounds);

                            BitmapFactory.Options opts = new BitmapFactory.Options();
                            Bitmap rBmp = BitmapFactory.decodeFile(tmpFile.getAbsolutePath(), opts);

                            int rotationAngle = 0;
                            if ((Build.MODEL.toUpperCase(Locale.KOREA).startsWith("SM-") || Build.MANUFACTURER.toUpperCase(Locale.KOREA).equals("SAMSUNG")) && mMode)
                                rotationAngle = 270;
                            else
                                rotationAngle = getCameraPhotoOrientation(mContext, Uri.parse(new File(tmpFile.getAbsolutePath()).toString()), tmp);

                            Matrix matrix = new Matrix();
                            matrix.postRotate(rotationAngle, (float) rBmp.getWidth(), (float) rBmp.getHeight());

                            Bitmap rotatedBmap = Bitmap.createBitmap(rBmp, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);

                            fos = new FileOutputStream(new File(path));
                            rotatedBmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.flush();

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (fos != null) fos.close();
                                tmpFile.delete();
                            } catch (Exception ex) {
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    ((FaceActivity) mContext).faceRecognition(path);
                }
            }
        });
    }

    /**
     * 카메라 회전률 반환
     * */
    public static int getCameraPhotoOrientation(Context context, Uri imageUri, String imagePath){
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            File imageFile = new File(imagePath);
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {
                case ExifInterface.ORIENTATION_NORMAL:
                    rotate = 0;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 0;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }
}
