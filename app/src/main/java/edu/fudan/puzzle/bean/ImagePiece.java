package edu.fudan.puzzle.bean;

import android.graphics.Bitmap;

public class ImagePiece {
    private int index = 0;//标记图片顺序|索引
    private Bitmap bitmap = null;//分割完的图片

    public ImagePiece(int index, Bitmap bitmap) {
        this.index = index;
        this.bitmap = bitmap;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }
}
