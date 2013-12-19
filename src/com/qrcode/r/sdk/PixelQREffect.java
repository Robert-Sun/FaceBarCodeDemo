package com.qrcode.r.sdk;

import android.graphics.*;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.QRCode;
import com.robert.image.compose.demo.QRCodeUtils;

/**
 * Created by michael on 13-12-19.
 */
public class PixelQREffect implements QREffectInterface {

    @Override
    public Bitmap makeEffectQRCode(String content, QRCodeOptions opt) {
        QRCode qrCode = QRCodeUtils.encodeQrcode(opt.qrContent);

        ByteMatrix input = qrCode.getMatrix();
        if (input == null) {
            throw new IllegalStateException();
        }
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        int qrWidth = inputWidth;
        int qrHeight = inputHeight;
        int outputWidth = Math.max(opt.defaultQRSize, qrWidth);
        int outputHeight = Math.max(opt.defaultQRSize, qrHeight);

        int multiple = Math.min(outputWidth / qrWidth, outputHeight / qrHeight);

        //四周各空两个点整
        int realWidth = multiple * (inputWidth + 4);
        int realHeight = multiple * (inputHeight + 4);

        int leftPadding = multiple * 2;
        int topPadding = multiple * 2;

        Bitmap pixelBt = mosaic(opt.backgroundBitmap, realWidth, realHeight, multiple);
        pixelBt.setHasAlpha(true);

        Bitmap out = Bitmap.createBitmap(realWidth, realHeight, Bitmap.Config.ARGB_8888);
        out.setHasAlpha(true);
        Canvas canvas = new Canvas(out);
        canvas.drawARGB(200, 255, 255, 255);
        Paint paint = new Paint();
        paint.setDither(true);
        paint.setAntiAlias(true);

        paint.setAlpha(150);
        ColorMatrix allMatrix = new ColorMatrix();
        ColorMatrix colorMatrix = new ColorMatrix();
//        //饱和度
        colorMatrix.setSaturation(6);
        allMatrix.postConcat(colorMatrix);
        //对比度
        float contrast = (float) ((10 + 64) / 128.0);
        ColorMatrix cMatrix = new ColorMatrix();
        cMatrix.set(new float[]{contrast, 0, 0, 0, 0, 0,
                                   contrast, 0, 0, 0,// 改变对比度
                                   0, 0, contrast, 0, 0, 0, 0, 0, 1, 0});
        allMatrix.postConcat(cMatrix);
//
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

        canvas.drawBitmap(pixelBt, 0, 0, paint);
        paint.setColorFilter(null);
        paint.setAlpha(160);

        Rect box = new Rect();

        for (int inputY = 0, outputY = topPadding; inputY < inputHeight; inputY++, outputY += multiple) {
            for (int inputX = 0, outputX = leftPadding; inputX < inputWidth; inputX++, outputX += multiple) {
                box.left = outputX;
                box.right = outputX + multiple;
                box.top = outputY;
                box.bottom = outputY + multiple;

                if (input.get(inputX, inputY) == 1) {
                    canvas.drawRect(new Rect(outputX, outputY, outputX + multiple, outputY + multiple), paint);
                }
            }
        }

        return out;
    }

    public static Bitmap mosaic(Bitmap original, int outWidth, int outHeight, int dot) {
        Bitmap bitmap = Bitmap.createScaledBitmap(original, outWidth, outHeight, false);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        Paint paint = new Paint();
        paint.setDither(true);

        int dotS = dot * dot;
        int w_count = outWidth / dot;
        int h_count = outHeight / dot;

        for (int i = 0; i < w_count; i++) {
            for (int j = 0; j < h_count; j++) {
                int rr = 0;
                int gg = 0;
                int bb = 0;
                for (int k = 0; k < dot; k++) {
                    for (int l = 0; l < dot; l++) {
                        int dotColor = bitmap.getPixel(i * dot + k, j * dot + l);
                        rr += Color.red(dotColor);
                        gg += Color.green(dotColor);
                        bb += Color.blue(dotColor);
                    }
                }
                rr = rr / dotS;
                gg = gg / dotS;
                bb = bb / dotS;
                paint.setColor(Color.rgb(rr, gg, bb));
                canvas.drawRect(new Rect(i * dot, j * dot, i * dot + dot, j * dot + dot), paint);
            }
        }

        return bitmap;
    }

}
