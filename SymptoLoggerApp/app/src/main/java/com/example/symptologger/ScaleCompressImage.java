package com.example.symptologger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/*
 *  Copyright 2018 Remi Arshad, Noni Hua, Jason Lee, Patrick Tamm, Kaiwen Zhang
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

public class ScaleCompressImage {
    private Context context;
    private Uri uri;
    private InputStream is;
    private final int heightLimit = 500;
    private final int widthLimit = 500;
    private final int downScaleFactor = 4;

    public ScaleCompressImage(Context context, Uri uri){
        this.uri = uri;
        this.context = context;
    }

    private int getScalingFactor(int width, int height){
        if ((width > widthLimit) || (height > heightLimit)){
            return downScaleFactor;
        }
        return 1;
    }

    public Bitmap scaleAndCompress(){

        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            is = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(is, null, options);
            is.close();
            is = context.getContentResolver().openInputStream(uri);
            options.inSampleSize = getScalingFactor(options.outWidth, options.outHeight);
            options.inJustDecodeBounds = false;
            Bitmap decoded = BitmapFactory.decodeStream(is, null, options);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            decoded.compress(Bitmap.CompressFormat.JPEG, 90, out);
            Bitmap decodedCompressed = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
            is.close();
            return decodedCompressed;
        }
        catch (Exception e){
            return null;
        }


    }
}
//package com.example.symptologger;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.net.Uri;
//
//import java.io.ByteArrayInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//
///*
// *  Copyright 2018 Remi Arshad, Noni Hua, Jason Lee, Patrick Tamm, Kaiwen Zhang
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
//
// *     http://www.apache.org/licenses/LICENSE-2.0
//
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// */
//
///**
// * ScaleCompressImage scales the image before storage.
// *
// * @author Remi Arshad
// */
//public class ScaleCompressImage {
//    private Context context;
//    private String path;
//    private final int heightLimit = 500;
//    private final int widthLimit = 500;
//    private final int downScaleFactor = 3;
//
//    public ScaleCompressImage(Context context, String path){
//        this.path = path;
//        this.context = context;
//    }
//
//    private int getScalingFactor(int width, int height){
//        if ((width > widthLimit) || (height > heightLimit)){
//            return downScaleFactor;
//        }
//        return 1;
//    }
//
//    public Bitmap scaleAndCompress(Uri uri){
////        BitmapFactory.decodeFile(path, options);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inJustDecodeBounds = true;
//
////        Uri uri = Uri.fromFile(new File(path));
//        InputStream is;
//        try{
//            is = context.getContentResolver().openInputStream(uri);
//            BitmapFactory.decodeStream(is, null, options);
//
//            options.inSampleSize = getScalingFactor(options.outWidth, options.outHeight);
//            options.inJustDecodeBounds = false;
//            Bitmap decoded = BitmapFactory.decodeStream(is, null, options);
////        Bitmap decoded = BitmapFactory.decodeFile(path, options);
//
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            decoded.compress(Bitmap.CompressFormat.JPEG, 85, out);
//            Bitmap decodedCompressed = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
//            return decodedCompressed;
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//            return null;
//        }
////        Bitmap decoded = BitmapFactory.decodeFile(path, options);
////
////        ByteArrayOutputStream out = new ByteArrayOutputStream();
////        decoded.compress(Bitmap.CompressFormat.JPEG, 85, out);
////        Bitmap decodedCompressed = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
////
////        return decodedCompressed;
//    }
//}
