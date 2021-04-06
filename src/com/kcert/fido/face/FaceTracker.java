package com.kcert.fido.face;

import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.kcert.fido.face.ui.camera.GraphicOverlay;

import android.graphics.PointF;

/**
 * 눈 위치와 상태를 추적하여 원본 비디오 위에 눈을 그려주는 기본 그래픽 관리
 */
public class FaceTracker extends Tracker<Face> {
    private static final float EYE_CLOSED_THRESHOLD = 0.4f;

    private GraphicOverlay mOverlay;
    private FaceGraphic mEyesGraphic;
    private Map<Integer, PointF> mPreviousProportions = new HashMap<>();

    private boolean mPreviousIsLeftOpen = true;
    private boolean mPreviousIsRightOpen = true;

    public FaceTracker(GraphicOverlay overlay) {
        mOverlay = overlay;
    }

    /**
     * GooglyEyesGraphic 재설정
     */
    @Override
    public void onNewItem(int id, Face face) {
        mEyesGraphic = new FaceGraphic(mOverlay);
    }

    /**
     * 눈의 위치와 상태 업데이트
     */
    @Override
    public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
        mOverlay.add(mEyesGraphic);

        updatePreviousProportions(face);

        PointF leftPosition = getLandmarkPosition(face, Landmark.LEFT_EYE);
        PointF rightPosition = getLandmarkPosition(face, Landmark.RIGHT_EYE);

        float leftOpenScore = face.getIsLeftEyeOpenProbability();   //왼쪽눈이 열려있을 확율(0.0~1.0)
        boolean isLeftOpen;

        //확률이 계산되지 않는경우
        if (leftOpenScore == Face.UNCOMPUTED_PROBABILITY) {
            isLeftOpen = mPreviousIsLeftOpen;
        } else {
            isLeftOpen = (leftOpenScore > EYE_CLOSED_THRESHOLD);
            mPreviousIsLeftOpen = isLeftOpen;
        }

        float rightOpenScore = face.getIsRightEyeOpenProbability();   //오른쪽눈이 열려있을 확율(0.0~1.0)
        boolean isRightOpen;
        if (rightOpenScore == Face.UNCOMPUTED_PROBABILITY) {
            isRightOpen = mPreviousIsRightOpen;
        } else {
            isRightOpen = (rightOpenScore > EYE_CLOSED_THRESHOLD);
            mPreviousIsRightOpen = isRightOpen;
        }

        mEyesGraphic.updateEyes(leftPosition, isLeftOpen, rightPosition, isRightOpen);
    }

    /**
     * 얼굴이 감지 되지 않을 때 그래픽 숨기기
     */
    @Override
    public void onMissing(FaceDetector.Detections<Face> detectionResults) {
        mOverlay.remove(mEyesGraphic);
    }

    /**
     * 얼굴이 잘 인식되지 않을 때 그래픽 숨기기
     */
    @Override
    public void onDone() {
        mOverlay.remove(mEyesGraphic);
    }

    private void updatePreviousProportions(Face face) {
        for (Landmark landmark : face.getLandmarks()) {
            PointF position = landmark.getPosition();
            float xProp = (position.x - face.getPosition().x) / face.getWidth();
            float yProp = (position.y - face.getPosition().y) / face.getHeight();
            mPreviousProportions.put(landmark.getType(), new PointF(xProp, yProp));
        }
    }

    /**
     * 랜드마크 위치를 찾거나, 존재하지 않는 경우 과거 위치를 기반으로 위치 추정
     */
    private PointF getLandmarkPosition(Face face, int landmarkId) {

        for (Landmark landmark : face.getLandmarks()) {
            if (landmark.getType() == landmarkId) {
                return landmark.getPosition();
            }
        }

        PointF prop = mPreviousProportions.get(landmarkId);
        if (prop == null) {
            return null;
        }

        float x = face.getPosition().x + (prop.x * face.getWidth());
        float y = face.getPosition().y + (prop.y * face.getHeight());
        return new PointF(x, y);
    }
}