package com.oclocksoftware.smarttvnotification.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.oclocksoftware.smarttvnotification.R;

/**
 * Created by babin on 12/26/2017.
 */

public class QRGenerator {
    public final static int QRcodeWidth = 500 ;
    static Bitmap bitmap ;

    public static Bitmap displayQR( String txt, Activity mActivity){
        try {
            bitmap=TextToImageEncode(txt,mActivity);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    static Bitmap TextToImageEncode(String Value, Activity mActivity) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        mActivity.getResources().getColor(R.color.QRCodeBlackColor):mActivity.getResources().getColor(R.color.QRCodeWhiteColor);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }
}
