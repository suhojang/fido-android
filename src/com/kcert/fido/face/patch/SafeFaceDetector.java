package com.kcert.fido.face.patch;

import android.graphics.ImageFormat;
import android.util.Log;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;

import java.nio.ByteBuffer;
import java.util.Arrays;
public class SafeFaceDetector extends Detector<Face> {
    private static final String TAG = "SafeFaceDetector";
    private Detector<Face> mDelegate;

    public SafeFaceDetector(Detector<Face> delegate) {
        mDelegate = delegate;
    }

    @Override
    public void release() {
        mDelegate.release();
    }

    @Override
    public SparseArray<Face> detect(Frame frame) {
        final int kMinDimension = 147;
        final int kDimensionLower = 640;
        int width = frame.getMetadata().getWidth();
        int height = frame.getMetadata().getHeight();

        if (height > (2 * kDimensionLower)) {
            double multiple = (double) height / (double) kDimensionLower;
            double lowerWidth = Math.floor((double) width / multiple);
            if (lowerWidth < kMinDimension) {
                int newWidth = (int) Math.ceil(kMinDimension * multiple);
                frame = padFrameRight(frame, newWidth);
            }
        } else if (width > (2 * kDimensionLower)) {
            double multiple = (double) width / (double) kDimensionLower;
            double lowerHeight = Math.floor((double) height / multiple);
            if (lowerHeight < kMinDimension) {
                int newHeight = (int) Math.ceil(kMinDimension * multiple);
                frame = padFrameBottom(frame, newHeight);
            }
        } else if (width < kMinDimension) {
            frame = padFrameRight(frame, kMinDimension);
        }

        return mDelegate.detect(frame);
    }

    @Override
    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    @Override
    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }

    private Frame padFrameRight(Frame originalFrame, int newWidth) {
        Frame.Metadata metadata = originalFrame.getMetadata();
        int width = metadata.getWidth();
        int height = metadata.getHeight();

        Log.i(TAG, "Padded image from: " + width + "x" + height + " to " + newWidth + "x" + height);

        ByteBuffer origBuffer = originalFrame.getGrayscaleImageData();
        int origOffset = origBuffer.arrayOffset();
        byte[] origBytes = origBuffer.array();

        ByteBuffer paddedBuffer = ByteBuffer.allocateDirect(newWidth * height);
        int paddedOffset = paddedBuffer.arrayOffset();
        byte[] paddedBytes = paddedBuffer.array();
        Arrays.fill(paddedBytes, (byte) 0);

        for (int y = 0; y < height; ++y) {
            int origStride = origOffset + y * width;
            int paddedStride = paddedOffset + y * newWidth;
            System.arraycopy(origBytes, origStride, paddedBytes, paddedStride, width);
        }

        return new Frame.Builder()
                .setImageData(paddedBuffer, newWidth, height, ImageFormat.NV21)
                .setId(metadata.getId())
                .setRotation(metadata.getRotation())
                .setTimestampMillis(metadata.getTimestampMillis())
                .build();
    }

    private Frame padFrameBottom(Frame originalFrame, int newHeight) {
        Frame.Metadata metadata = originalFrame.getMetadata();
        int width = metadata.getWidth();
        int height = metadata.getHeight();

        Log.i(TAG, "Padded image from: " + width + "x" + height + " to " + width + "x" + newHeight);

        ByteBuffer origBuffer = originalFrame.getGrayscaleImageData();
        int origOffset = origBuffer.arrayOffset();
        byte[] origBytes = origBuffer.array();

        ByteBuffer paddedBuffer = ByteBuffer.allocateDirect(width * newHeight);
        int paddedOffset = paddedBuffer.arrayOffset();
        byte[] paddedBytes = paddedBuffer.array();
        Arrays.fill(paddedBytes, (byte) 0);

        for (int y = 0; y < height; ++y) {
            int origStride = origOffset + y * width;
            int paddedStride = paddedOffset + y * width;
            System.arraycopy(origBytes, origStride, paddedBytes, paddedStride, width);
        }

        return new Frame.Builder()
                .setImageData(paddedBuffer, width, newHeight, ImageFormat.NV21)
                .setId(metadata.getId())
                .setRotation(metadata.getRotation())
                .setTimestampMillis(metadata.getTimestampMillis())
                .build();
    }
}
