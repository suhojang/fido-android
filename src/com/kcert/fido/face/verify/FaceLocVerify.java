package com.kcert.fido.face.verify;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.kcert.fido.face.patch.SafeFaceDetector;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.SparseArray;

public class FaceLocVerify {
	private static FaceLocVerify instance;
	
	public FaceLocVerify() {
	}
	
	public static FaceLocVerify getInstance(){
		synchronized (FaceLocVerify.class) {
			if(instance==null){
				instance	= new FaceLocVerify();
			}
		}
		return instance;
	}
	
	public boolean getFaceVerify(Context context, String path1, String path2){
		Map<String,Double> original	= setFaceLocation(getLocationVO(getLocationInfo(context, path1)));
		Map<String,Double> location	= setFaceLocation(getLocationVO(getLocationInfo(context, path2)));
		
		return faceVerifyAlgorithm(original, location);
	}
	
	private Map<String,Integer> getLocationInfo(Context context, String path){
		Map<String,Integer> map	= new HashMap<String,Integer>();
		InputStream stream = null;
		
        try {
            stream = new FileInputStream(new File(path));

            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            FaceDetector detector = new FaceDetector.Builder(context)
                    .setTrackingEnabled(false)
                    .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                    .build();
            Detector<Face> safeDetector = new SafeFaceDetector(detector);

            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<Face> faces = safeDetector.detect(frame);

            for (int i = 0; i < faces.size(); ++i) {
                Face face = faces.valueAt(i);
                for (Landmark landmark : face.getLandmarks()) {
                    int x = (int) (landmark.getPosition().x);
                    int y = (int) (landmark.getPosition().y);
                    
                    map.put(landmark.getType()+"_x", x);
                    map.put(landmark.getType()+"_y", y);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        
		return map;
	}
	
	private static double getDistance(int x, int y, int x1, int y1) {
        return Math.sqrt(Math.pow(Math.abs(x1-x),2) + Math.pow(Math.abs(y1-y), 2));
    }
	
	private static double getRatio(double area1, double area2){
		return Double.parseDouble(new DecimalFormat("#.##").format(area2 / area1));
	}
	
	public FaceLocationVO getLocationVO(Map<String,Integer> map){
		FaceLocationVO faceVO	= new FaceLocationVO();
		
		faceVO.setLeft_eye_x(map.get(Landmark.LEFT_EYE+"_x"));
		faceVO.setLeft_eye_y(map.get(Landmark.LEFT_EYE+"_y"));
		faceVO.setRight_eye_x(map.get(Landmark.RIGHT_EYE+"_x"));
		faceVO.setRight_eye_y(map.get(Landmark.RIGHT_EYE+"_y"));
		faceVO.setLeft_mouse_x(map.get(Landmark.LEFT_MOUTH+"_x"));
		faceVO.setLeft_mouse_y(map.get(Landmark.LEFT_MOUTH+"_y"));
		faceVO.setRight_mouse_x(map.get(Landmark.RIGHT_MOUTH+"_x"));
		faceVO.setRight_mouse_y(map.get(Landmark.RIGHT_MOUTH+"_y"));
		faceVO.setNose_x(map.get(Landmark.NOSE_BASE+"_x"));
		faceVO.setNose_y(map.get(Landmark.NOSE_BASE+"_y"));
		
		return faceVO;
	}
	
	public Map<String,Double> setFaceLocation(FaceLocationVO faceVO){
		Map<String,Double> map	= new HashMap<String,Double>(); 
		map.put("LEFT_EYE_INTERVAL", 	getDistance(faceVO.getNose_x(), faceVO.getNose_y(), faceVO.getLeft_eye_x(), faceVO.getLeft_eye_y()));
		map.put("RIGHT_EYE_INTERVAL", 	getDistance(faceVO.getNose_x(), faceVO.getNose_y(), faceVO.getRight_eye_x(), faceVO.getRight_eye_y()));
		map.put("LEFT_MOUSE_INTERVAL", 	getDistance(faceVO.getNose_x(), faceVO.getNose_y(), faceVO.getLeft_mouse_x(), faceVO.getLeft_mouse_y()));
		map.put("RIGHT_MOUSE_INTERVAL", getDistance(faceVO.getNose_x(), faceVO.getNose_y(), faceVO.getRight_mouse_x(), faceVO.getRight_mouse_y()));
		map.put("BOTTOM_MOUSE_INTERVAL",getDistance(faceVO.getNose_x(), faceVO.getNose_y(), faceVO.getBottom_mouse_x(), faceVO.getBottom_mouse_y()));
		map.put("WIDTH", 	getDistance(faceVO.getLeft_eye_x(), faceVO.getLeft_eye_y(), faceVO.getRight_eye_x(), faceVO.getRight_eye_y()));
		map.put("HEIGHT", 	(double) Math.round(Math.sqrt(Math.round((Math.pow(map.get("LEFT_EYE_INTERVAL"), 2)) - Math.round(Math.pow(map.get("WIDTH"), 2))))) + map.get("BOTTOM_MOUSE_INTERVAL"));
		map.put("AREA", 	(double) ((map.get("WIDTH") * map.get("HEIGHT")) / 2));
		
		return map;
	}
	
	public boolean faceVerifyAlgorithm(Map<String,Double> original, Map<String,Double> loc){
		double ratio	=  getRatio(original.get("AREA"), loc.get("AREA"));
		
		double left_eye		= loc.get("left_eye_interval") / (original.get("left_eye_interval")*ratio);
		double right_eye	= loc.get("right_eye_interval") / (original.get("right_eye_interval")*ratio);
		double left_mouse	= loc.get("left_mouse_interval") / (original.get("left_mouse_interval")*ratio);
		double right_mouse	= loc.get("right_mouse_interval") / (original.get("right_mouse_interval")*ratio);
		double bottom_mouse	= loc.get("bottom_mouse_interval") / (original.get("bottom_mouse_interval")*ratio);
		double width		= loc.get("width") / (original.get("width")*ratio);
		double height		= loc.get("height") / (original.get("height")*ratio);
		double area			= loc.get("area") / (original.get("area")*ratio);
		
		int matchRate	= Integer.parseInt(new DecimalFormat("##").format(((left_eye + right_eye + left_mouse + right_mouse + bottom_mouse + width + height + area) * 100)/8));
		
		return matchRate >= 75 && matchRate <= 100;
	}
}
