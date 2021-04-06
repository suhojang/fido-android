package com.kcert.fido.face;

import com.kcert.fido.face.ui.camera.GraphicOverlay;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Handler;


/**
 * 인식되는 눈에 이미지 렌더링
 */
class FaceGraphic extends GraphicOverlay.Graphic {
    private static final float EYE_RADIUS_PROPORTION = 0.45f;
    private static final float IRIS_RADIUS_PROPORTION = EYE_RADIUS_PROPORTION / 2.0f;

    private Paint mEyeWhitesPaint;
    private Paint mEyeIrisPaint;
    private Paint mEyeOutlinePaint;
    private Paint mEyeLidPaint;

    private EyePhysics mLeftPhysics = new EyePhysics();
    private EyePhysics mRightPhysics = new EyePhysics();

    private volatile PointF mLeftPosition;
    private volatile boolean mLeftOpen;

    private volatile PointF mRightPosition;
    private volatile boolean mRightOpen;

    private int closeEyesCount  = 0;
    private boolean closeEyes   = false;

    public FaceGraphic(GraphicOverlay overlay) {
        super(overlay);
        //눈을 떴을때
        mEyeWhitesPaint = new Paint();
        mEyeWhitesPaint.setColor(Color.WHITE);
        mEyeWhitesPaint.setStyle(Paint.Style.FILL);

        //눈을 감았을때
        mEyeLidPaint = new Paint();
        mEyeLidPaint.setColor(Color.YELLOW);
        mEyeLidPaint.setStyle(Paint.Style.FILL);

        //눈동자 색상
        mEyeIrisPaint = new Paint();
        mEyeIrisPaint.setColor(Color.BLACK);
        mEyeIrisPaint.setStyle(Paint.Style.FILL);

        //테두리 색상
        mEyeOutlinePaint = new Paint();
        mEyeOutlinePaint.setColor(Color.BLACK);
        mEyeOutlinePaint.setStyle(Paint.Style.STROKE);
        mEyeOutlinePaint.setStrokeWidth(5);
    }

    /**
     * 눈의 위치와 상태를 갱신
     * 오버레이 무효화
     */
    public void updateEyes(PointF leftPosition, boolean leftOpen, PointF rightPosition, boolean rightOpen) {
        mLeftPosition = leftPosition;
        mLeftOpen = leftOpen;

        mRightPosition = rightPosition;
        mRightOpen = rightOpen;

        postInvalidate();
    }

    /**
     * 현재 눈 상태를 canvas에 그린다.
     */
    @Override
    public void draw(Canvas canvas) {
        if (closeEyes)
            return;
        if (!mLeftOpen && !mRightOpen) {
            closeEyesCount++;
            if (closeEyesCount > 12) {
                closeEyes = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        capture();
                    }
                }, 500);
                return;
            }
        }
        PointF detectLeftPosition = mLeftPosition;
        PointF detectRightPosition = mRightPosition;
        if ((detectLeftPosition == null) || (detectRightPosition == null)) {
            return;
        }

        PointF leftPosition = new PointF(translateX(detectLeftPosition.x), translateY(detectLeftPosition.y));
        PointF rightPosition = new PointF(translateX(detectRightPosition.x), translateY(detectRightPosition.y));

        float distance = (float) Math.sqrt(Math.pow(rightPosition.x - leftPosition.x, 2) + Math.pow(rightPosition.y - leftPosition.y, 2));
        float eyeRadius = EYE_RADIUS_PROPORTION * distance;
        float irisRadius = IRIS_RADIUS_PROPORTION * distance;

        //왼쪽 눈 상태 그리기
        PointF leftIrisPosition = mLeftPhysics.nextIrisPosition(leftPosition, eyeRadius, irisRadius);
        drawEye(canvas, leftPosition, eyeRadius, leftIrisPosition, irisRadius, mLeftOpen);

        //오른쪽 눈 상태 그리기
        PointF rightIrisPosition = mRightPhysics.nextIrisPosition(rightPosition, eyeRadius, irisRadius);
        drawEye(canvas, rightPosition, eyeRadius, rightIrisPosition, irisRadius, mRightOpen);
    }

    /**
     * 눈의 상태를 canvas를 이용하여 그린다.
     */
    private void drawEye(Canvas canvas, PointF eyePosition, float eyeRadius, PointF irisPosition, float irisRadius, boolean isOpen) {
        if (isOpen) {
            canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeWhitesPaint);
            canvas.drawCircle(irisPosition.x, irisPosition.y, irisRadius, mEyeIrisPaint);
        } else {
            canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeLidPaint);
            float y = eyePosition.y;
            float start = eyePosition.x - eyeRadius;
            float end = eyePosition.x + eyeRadius;
            canvas.drawLine(start, y, end, y, mEyeOutlinePaint);
        }
        canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeOutlinePaint);
    }
}
