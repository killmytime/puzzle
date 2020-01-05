package edu.fudan.puzzle.layout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.fudan.puzzle.R;
import edu.fudan.puzzle.bean.ImagePiece;
import edu.fudan.puzzle.utils.ImageSplitter;

public class PuzzleLayout extends RelativeLayout implements View.OnClickListener {
    //Configuration
    private int mColumn = 2;//列数目
    private int mRow = 2;//行数目
    private int mWidth;
    private int mHeight;
    private int mPadding;
    private int mMargin = 3;

    //Images
    private ImageView[] puzzleItems;
    private int mItemWidth;
    private int mItemHeight;
    private Bitmap mBitmap;
    private List<ImagePiece> mItemBitmaps;

    //init
    private boolean isInit;

    //两次点击的块
    private ImageView mFirst;
    private ImageView mSecond;

    //animation
    private boolean isAnimation;
    private RelativeLayout mAnimLayout;


    public PuzzleLayout(Context context) {
        this(context, null);
    }

    public PuzzleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PuzzleLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //把设置的margin值转换为dp
        mMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mMargin, getResources().getDisplayMetrics());
        // 设置Layout的内边距，四边一致，设置为四内边距中的最小值
        mPadding = Collections.max(Arrays.asList(getPaddingBottom(), getPaddingLeft(), getPaddingTop(), getPaddingRight()));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 获得游戏布局的边长
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredWidth();
        if (!isInit) {
            initBitmap();
            initItem();
        }
        isInit = true;
        setMeasuredDimension(mWidth, mHeight);
    }

    private void initBitmap() {
        if (mBitmap == null)
            mBitmap = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.test);
        mItemBitmaps = ImageSplitter.split(scale(mBitmap, mWidth, mHeight), mColumn, mRow);
        //打乱图片顺序
        Collections.shuffle(mItemBitmaps);
    }

    private void initItem() {
        // 获得Item的宽度
        int childWidth = (mWidth - mPadding * 2 - mMargin * (mColumn - 1)) / mColumn;
        int childHeight = (mHeight - mPadding * 2 - mMargin * (mRow - 1)) / mRow;
        mItemWidth = Math.min(childWidth, mItemBitmaps.get(0).getBitmap().getWidth());
        mItemHeight = Math.min(childHeight, mItemBitmaps.get(0).getBitmap().getHeight());
        puzzleItems = new ImageView[mColumn * mRow];
        // 放置Item
        for (int i = 0; i < puzzleItems.length; i++) {
            ImageView item = new ImageView(getContext());

            item.setOnClickListener(this);
            item.setImageBitmap(mItemBitmaps.get(i).getBitmap());
            item.setId(i + 1);
            item.setTag(i + "_" + mItemBitmaps.get(i).getIndex());
            puzzleItems[i] = item;
            RelativeLayout.LayoutParams lp = new LayoutParams(mItemWidth, mItemHeight);
            // 设置横向边距,不是最后一列
            if ((i + 1) % mColumn != 0) {
                lp.rightMargin = mMargin;
            }
            // 如果不是第一列
            if (i % mColumn != 0) {
                lp.addRule(RelativeLayout.RIGHT_OF,
                        puzzleItems[i - 1].getId());
            }
            // 如果不是第一行,设置纵向边距
            if ((i + 1) > mColumn) {
                lp.topMargin = mMargin;
                lp.addRule(RelativeLayout.BELOW,
                        puzzleItems[i - mColumn].getId());
            }
            addView(item, lp);
        }
    }

    @Override
    public void onClick(View view) {
//        Log.d("TAG", "onClick: " + view.getTag());
        if (isAnimation)
            return;
        //如果两次点击的是同一个View
        if (mFirst == view) {
            mFirst.setColorFilter(null);
            mFirst = null;
            return;
        }
        //点击第一个View
        if (mFirst == null) {
            mFirst = (ImageView) view;
            //Todo 过滤色的选择
            mFirst.setColorFilter(0x55FF0000);
        } else {//点击第二个View
            mSecond = (ImageView) view;
            switchBlock();
        }
    }

    private void switchBlock() {
        mFirst.setColorFilter(null);
        if (mAnimLayout == null) {
            mAnimLayout = new RelativeLayout(getContext());
            addView(mAnimLayout);
        }
        // 添加FirstView
        ImageView first = new ImageView(getContext());
        first.setImageBitmap(mItemBitmaps.get(getImageIndexByTag((String) mFirst.getTag())).getBitmap());
        LayoutParams lp = new LayoutParams(mItemWidth, mItemHeight);
        lp.leftMargin = mFirst.getLeft() - mPadding;
        lp.topMargin = mFirst.getTop() - mPadding;
        first.setLayoutParams(lp);
        mAnimLayout.addView(first);
        // 添加SecondView
        ImageView second = new ImageView(getContext());
        second.setImageBitmap(mItemBitmaps
                .get(getImageIndexByTag((String) mSecond.getTag())).getBitmap());
        LayoutParams lp2 = new LayoutParams(mItemWidth, mItemHeight);
        lp2.leftMargin = mSecond.getLeft() - mPadding;
        lp2.topMargin = mSecond.getTop() - mPadding;
        second.setLayoutParams(lp2);
        mAnimLayout.addView(second);

        // 设置动画
        TranslateAnimation anim = new TranslateAnimation(0, mSecond.getLeft()
                - mFirst.getLeft(), 0, mSecond.getTop() - mFirst.getTop());
        anim.setDuration(300);
        anim.setFillAfter(true);
        first.startAnimation(anim);

        TranslateAnimation animSecond = new TranslateAnimation(0,
                mFirst.getLeft() - mSecond.getLeft(), 0, mFirst.getTop()
                - mSecond.getTop());
        animSecond.setDuration(300);
        animSecond.setFillAfter(true);
        second.startAnimation(animSecond);
        // 添加动画监听
        anim.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                isAnimation = true;
                mFirst.setVisibility(INVISIBLE);
                mSecond.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                String firstTag = (String) mFirst.getTag();
                String secondTag = (String) mSecond.getTag();

                String[] firstParams = firstTag.split("_");
                String[] secondParams = secondTag.split("_");

                mFirst.setImageBitmap(mItemBitmaps.get(Integer.parseInt(secondParams[0])).getBitmap());
                mSecond.setImageBitmap(mItemBitmaps.get(Integer.parseInt(firstParams[0])).getBitmap());

                mFirst.setTag(secondTag);
                mSecond.setTag(firstTag);
                mFirst.setVisibility(VISIBLE);
                mSecond.setVisibility(VISIBLE);
                mFirst = mSecond = null;
                mAnimLayout.removeAllViews();
                isAnimation = false;
                //进行游戏胜利判断
                checkSuccess();
            }
        });
    }

    /**
     * 用来判断游戏是否成功
     */
    private void checkSuccess() {
        boolean isSuccess = true;
        for (int i = 0; i < puzzleItems.length; i++) {
            if (getIndexByTag((String) puzzleItems[i].getTag()) != i)
                isSuccess = false;
        }
        if (isSuccess) {
            Toast.makeText(getContext(), "Success , enter new game !",
                    Toast.LENGTH_LONG).show();
            nextGame();
        }
    }

    /**
     * 进入下一关
     */
    private void nextGame() {
        this.removeAllViews();
        mAnimLayout = null;
//        mColumn++;
//        mRow++;
        initBitmap();
        initItem();
    }


    /**
     * 获得存储在mItemBitmaps中存储图片的角标
     *
     * @param tag the tag
     * @return the image index by tag
     */
    private int getImageIndexByTag(String tag) {
        String[] split = tag.split("_");
        return Integer.parseInt(split[0]);
    }

    /**
     * 获得图片的真正索引
     *
     * @param tag the tag
     * @return the index by tag
     */
    private int getIndexByTag(String tag) {
        String[] split = tag.split("_");
        return Integer.parseInt(split[1]);
    }

    /**
     * 图片缩放
     *
     * @param bitmap    图片
     * @param newWidth  新的宽度
     * @param newHeight 新的高度
     * @return 缩放后的图片
     */
    private Bitmap scale(Bitmap bitmap, int newWidth, int newHeight) {
        // 计算缩放比例
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        return Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, true);
    }
}
