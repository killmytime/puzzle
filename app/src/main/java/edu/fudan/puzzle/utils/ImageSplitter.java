package edu.fudan.puzzle.utils;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.fudan.puzzle.bean.ImagePiece;

public class ImageSplitter {
    public static List<ImagePiece> split(Bitmap bitmap, int column,int row) {

        List<ImagePiece> pieces = new ArrayList<>(column * row);

        int pieceWidth = bitmap.getWidth()/ column;
        int pieceHeight = bitmap.getHeight()/row;
        for (int y = 0; y < row; y++) {
            for (int x = 0; x < column; x++) {
                pieces.add(new ImagePiece(x + y * column, Bitmap.createBitmap(bitmap, x*pieceWidth, y*pieceHeight,
                        pieceWidth, pieceHeight)));
//                Log.e("", String.valueOf(x+y*column));
            }
        }
        return pieces;
    }
}
