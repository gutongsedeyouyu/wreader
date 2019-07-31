package org.wreader.reader.reader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.graphics.drawable.GradientDrawable;
import android.widget.Scroller;

class ReaderPageTurningAnimator {
    static final int STYLE_PAGE_CURL = 0;
    static final int STYLE_COVER = 1;

    private static final ReaderPageTurningAnimator INSTANCE = new ReaderPageTurningAnimator();

    private int style;
    private final CurlPageTurningAnimator curlPageTurningAnimator = new CurlPageTurningAnimator();
    private final CoverPageTurningAnimator coverPageTurningAnimator = new CoverPageTurningAnimator();

    private ReaderPageTurningAnimator() {
    }

    static ReaderPageTurningAnimator getInstance() {
        return INSTANCE;
    }

    void setStyle(int style) {
        this.style = style;
    }

    void setColorSetting(ReaderColorSetting colorSetting) {
        curlPageTurningAnimator.setColorSetting(colorSetting);
        coverPageTurningAnimator.setColorSetting(colorSetting);
    }

    void draw(Canvas canvas, Bitmap bottomPageBitmap, Bitmap topPageBitmap,
              PointF actionDownPoint, PointF actionMovePoint, float actionDeltaX) {
        switch (style) {
            case STYLE_PAGE_CURL: {
                curlPageTurningAnimator.draw(canvas, bottomPageBitmap, topPageBitmap,
                                             actionDownPoint, actionMovePoint, actionDeltaX);
                break;
            }
            case STYLE_COVER: {
                coverPageTurningAnimator.draw(canvas, bottomPageBitmap, topPageBitmap,
                                              actionDownPoint, actionMovePoint, actionDeltaX);
                break;
            }
            default: {
                break;
            }
        }
    }

    void startScroll(Scroller scroller, int width, int height,
                     PointF actionDownPoint, PointF actionMovePoint, int actionDirection) {
        switch (style) {
            case STYLE_PAGE_CURL: {
                curlPageTurningAnimator.startScroll(scroller, width, height,
                                                    actionDownPoint, actionMovePoint, actionDirection);
                break;
            }
            case STYLE_COVER: {
                coverPageTurningAnimator.startScroll(scroller, width, height,
                                                     actionDownPoint, actionMovePoint, actionDirection);
                break;
            }
            default: {
                break;
            }
        }
    }

    private static class CurlPageTurningAnimator {
        private static final boolean DEBUG = false;

        private final PointF m1 = new PointF();
        private final PointF m2 = new PointF();
        private final float m2Offset = 6 * Resources.getSystem().getDisplayMetrics().density;
        private final PointF n = new PointF();
        private final PointF a = new PointF();
        private final PointF b1 = new PointF();
        private final PointF b2 = new PointF();
        private final PointF b3 = new PointF();
        private final PointF c = new PointF();
        private final PointF d1 = new PointF();
        private final PointF d2 = new PointF();
        private final PointF d3 = new PointF();
        private final PointF e = new PointF();
        private final PointF f1 = new PointF();
        private final PointF f2 = new PointF();
        private final PointF f3 = new PointF();
        private final PointF g = new PointF();

        private final Matrix topPageBackMatrix = new Matrix();
        private final float[] topPageBackMatrixValues = new float[] {0.0f, 0.0f, 0.0f,
                                                                     0.0f, 0.0f, 0.0f,
                                                                     0.0f, 0.0f, 1.0f};
        private final Paint topPageBackPaint = new Paint();

        private final GradientDrawable topPageShadowDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {0x00000000, 0x00000000});
        private final GradientDrawable topPageBackShadowDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {0x00000000, 0x00000000});
        private final GradientDrawable bottomPageShadowDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {0x00000000, 0x00000000});

        private final Path path1 = new Path();
        private final Path path2 = new Path();

        private CurlPageTurningAnimator() {
            topPageBackPaint.setStyle(Paint.Style.FILL);
            topPageBackPaint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(
                    new float[] {0.55f, 0.0f, 0.0f, 0.0f, 80.0f,
                                 0.0f, 0.55f, 0.0f, 0.0f, 80.0f,
                                 0.0f, 0.0f, 0.55f, 0.0f, 80.0f,
                                 0.0f, 0.0f, 0.0f, 0.2f, 0.0f})));
        }

        void setColorSetting(ReaderColorSetting colorSetting) {
            if (colorSetting.isLightBackground) {
                topPageShadowDrawable.setColors(new int[] {0x66333333, 0x00000000});
                topPageBackShadowDrawable.setColors(new int[] {0x99333333, 0x00000000});
                bottomPageShadowDrawable.setColors(new int[] {0xff111111, 0x00000000});
            } else {
                topPageShadowDrawable.setColors(new int[] {0x66111111, 0x00000000});
                topPageBackShadowDrawable.setColors(new int[] {0x99222222, 0x00000000});
                bottomPageShadowDrawable.setColors(new int[] {0xff111111, 0x00000000});
            }
        }

        void draw(Canvas canvas, Bitmap bottomPageBitmap, Bitmap topPageBitmap,
                  PointF actionDownPoint, PointF actionMovePoint, float actionDeltaX) {
            calculate(canvas.getWidth(), canvas.getHeight(),
                      actionDownPoint, actionMovePoint, (actionDeltaX < 0.0f ? -1 : 1));
            drawTopPage(canvas, topPageBitmap);
            drawTopPageShadow1(canvas);
            drawTopPageShadow2(canvas);
            drawTopPageBack(canvas, topPageBitmap);
            drawBottomPage(canvas, bottomPageBitmap);
            if (DEBUG) {
                drawDebugElements(canvas);
            }
        }

        void startScroll(Scroller scroller, int width, int height,
                         PointF actionDownPoint, PointF actionMovePoint, int actionDirection) {
            calculate(width, height,
                      actionDownPoint, actionMovePoint, actionDirection);
            int startX = (int) m1.x;
            int startY = (int) m1.y;
            int distanceX = (actionDirection > 0) ? (width - startX) : (-startX - width / 2);
            int distanceY = (startY < height / 2) ? -startY : (height - startY);
            scroller.startScroll(startX, startY, distanceX, distanceY);
        }

        private void calculate(int width, int height,
                               PointF actionDownPoint, PointF actionMovePoint, int actionDirection) {
            //
            // m1, n, m2, d1, d3, d2, a
            //
            m1.x = actionMovePoint.x;
            if (actionDirection > 0 || actionMovePoint.y <= 0.0f || actionMovePoint.y >= height) {
                m1.y = 0.0001f;
            } else if (actionDownPoint.y < height * 0.3f) {
                m1.y = Math.min(actionMovePoint.y, height * 0.3f);
            } else if (actionDownPoint.y > height * 0.7f) {
                m1.y = Math.max(actionMovePoint.y, height * 0.7f);
            } else {
                m1.y = 0.0001f;
            }
            for (int i = 0; i < 2; i++) {
                n.x = (m1.x >= 0.0f) ? width : (width + m1.x);
                n.y = (m1.y > height / 2) ? height : 0.0f;
                m2.x = (float) (m1.x - m2Offset * (n.x - m1.x) / Math.hypot(n.x - m1.x, n.y - m1.y));
                m2.y = (float) (m1.y - m2Offset * (n.y - m1.y) / Math.hypot(n.x - m1.x, n.y - m1.y));
                d1.x = m1.x / 2 + n.x / 2;
                d1.y = m1.y / 2 + n.y / 2;
                d3.x = d1.x / 2 + m1.x / 2;
                d3.y = d1.y / 2 + m1.y / 2;
                d2.x = d1.x / 2 + d3.x / 2;
                d2.y = d1.y / 2 + d3.y / 2;
                a.x = d3.x - (n.y - d3.y) * (n.y - d3.y) / (n.x - d3.x);
                a.y = n.y;
                if (m1.x > 0.0f && a.x < 0.0f) {
                    b1.x = width / 3;
                    d1.y = n.y + (n.y > 0.0f ? -1 : 1) * ((float) Math.sqrt((d1.x - b1.x) * (n.x - d1.x)));
                    m1.y = d1.y * 2 - n.y;
                } else {
                    break;
                }
            }
            //
            // b1, b3, b2, c
            //
            b1.x = a.x + (n.x - a.x) / 3;
            b1.y = a.y;
            b3.x = a.x + (d3.x - a.x) / 3;
            b3.y = a.y - (a.y - d3.y) / 3;
            b2.x = b1.x / 2 + b3.x / 2;
            b2.y = b1.y / 2 + b3.y / 2;
            c.x = b3.x / 2 + d3.x / 2;
            c.y = b3.y / 2 + d3.y / 2;
            //
            // g, f1, f3, f2, e
            //
            g.x = n.x;
            g.y = d3.y - (n.x - d3.x) * (n.x - d3.x) / (n.y - d3.y);
            f1.x = g.x;
            f1.y = g.y + (n.y - g.y) / 3;
            f3.x = g.x - (g.x - d3.x) / 3;
            f3.y = g.y + (d3.y - g.y) / 3;
            f2.x = f1.x / 2 + f3.x / 2;
            f2.y = f1.y / 2 + f3.y / 2;
            e.x = d3.x / 2 + f3.x / 2;
            e.y = d3.y / 2 + f3.y / 2;
            //
            // path1
            //
            path1.reset();
            path1.moveTo(n.x, n.y);
            path1.lineTo(a.x, a.y);
            path1.quadTo(b1.x, b1.y, c.x, c.y);
            path1.lineTo(m1.x, m1.y);
            path1.lineTo(e.x, e.y);
            path1.quadTo(f1.x, f1.y, g.x, g.y);
            path1.lineTo(n.x, height - n.y);
            path1.lineTo(width, height - n.y);
            path1.lineTo(width, n.y);
            path1.close();
        }

        private void drawTopPage(Canvas canvas, Bitmap topPageBitmap) {
            canvas.save();
            canvas.clipPath(path1, Region.Op.DIFFERENCE);
            canvas.drawBitmap(topPageBitmap, 0.0f, 0.0f, null);
            canvas.restore();
        }

        private void drawTopPageShadow1(Canvas canvas) {
            //
            // Calculate shadow bounds and rotate degrees
            //
            float shadowRotateDegrees;
            if (n.y == 0.0f) {
                topPageShadowDrawable.setBounds((int) m1.x,
                        (int) (m1.y - m2Offset),
                        (int) (m1.x + m2Offset * Math.hypot(d1.x - b1.x, d1.y - b1.y) / (n.x - b1.x)),
                        (int) (m1.y + Math.hypot(m1.x - b1.x, m1.y - b1.y)));
                shadowRotateDegrees = 270.0f + (float) Math.toDegrees(Math.atan2(b1.y - m1.y, b1.x - m1.x));
            } else {
                topPageShadowDrawable.setBounds((int) m1.x,
                        (int) (m1.y - Math.hypot(m1.x - b1.x, m1.y - b1.y)),
                        (int) (m1.x + m2Offset * Math.hypot(d1.x - b1.x, d1.y - b1.y) / (n.x - b1.x)),
                        (int) (m1.y + m2Offset));
                shadowRotateDegrees = 90.0f + (float) Math.toDegrees(Math.atan2(b1.y - m1.y, b1.x - m1.x));
            }
            //
            // Draw shadow
            //
            path2.reset();
            path2.moveTo(m2.x, m2.y);
            path2.lineTo(m1.x, m1.y);
            path2.lineTo(b1.x, b1.y);
            path2.lineTo(b1.x - m1.x + m2.x, b1.y - m1.y + m2.y);
            path2.close();
            canvas.save();
            canvas.clipPath(path1, Region.Op.DIFFERENCE);
            canvas.clipPath(path2, Region.Op.INTERSECT);
            canvas.rotate(shadowRotateDegrees, m1.x, m1.y);
            topPageShadowDrawable.draw(canvas);
            canvas.restore();
        }

        private void drawTopPageShadow2(Canvas canvas) {
            //
            // Calculate shadow bounds and rotate degrees
            //
            float shadowRotateDegrees;
            if (n.y == 0.0f) {
                topPageShadowDrawable.setBounds((int) m1.x,
                        (int) (m1.y - Math.hypot(m1.x - f1.x, m1.y - f1.y)),
                        (int) (m1.x + m2Offset * Math.hypot(d1.x - n.x, d1.y - n.y) / (n.x - b1.x)),
                        (int) (m1.y + m2Offset));
                shadowRotateDegrees = 180.0f - (float) Math.toDegrees(Math.atan2(f1.x - m1.x, f1.y - m1.y));
            } else {
                topPageShadowDrawable.setBounds((int) m1.x,
                        (int) (m1.y - m2Offset),
                        (int) (m1.x + m2Offset * Math.hypot(d1.x - n.x, d1.y - n.y) / (n.x - b1.x)),
                        (int) (m1.y + Math.hypot(m1.x - f1.x, m1.y - f1.y)));
                shadowRotateDegrees = 360.0f - (float) Math.toDegrees(Math.atan2(f1.x - m1.x, f1.y - m1.y));
            }
            //
            // Draw shadow
            //
            path2.reset();
            path2.moveTo(m2.x, m2.y);
            path2.lineTo(m1.x, m1.y);
            path2.lineTo(f1.x, f1.y);
            path2.lineTo(f1.x - m1.x + m2.x, f1.y - m1.y + m2.y);
            path2.close();
            canvas.save();
            canvas.clipPath(path1, Region.Op.DIFFERENCE);
            canvas.clipPath(path2, Region.Op.INTERSECT);
            canvas.rotate(shadowRotateDegrees, m1.x, m1.y);
            topPageShadowDrawable.draw(canvas);
            canvas.restore();
        }

        private void drawTopPageBack(Canvas canvas, Bitmap topPageBitmap) {
            //
            // Calculate matrix
            //
            float t1 = (float) Math.hypot(n.x - b1.x, n.y - f1.y);
            float t2 = (n.x - b1.x) / t1;
            float t3 = (f1.y - n.y) / t1;
            topPageBackMatrixValues[0] = 1 - 2 * t3 * t3;
            topPageBackMatrixValues[1] = 2 * t2 * t3;
            topPageBackMatrixValues[3] = 2 * t2 * t3;
            topPageBackMatrixValues[4] = 1 - 2 * t2 * t2;
            topPageBackMatrix.reset();
            topPageBackMatrix.setValues(topPageBackMatrixValues);
            topPageBackMatrix.preTranslate(-b1.x, -b1.y);
            topPageBackMatrix.postTranslate(b1.x, b1.y);
            //
            // Calculate shadow bounds and rotate degrees
            //
            float shadowRotateDegrees;
            if (n.y == 0.0f) {
                topPageBackShadowDrawable.setBounds((int) d2.x,
                        (int) (d2.y - Math.hypot(d2.x - f2.x, d2.y - f2.y)),
                        (int) (d2.x + Math.hypot(d2.x - d3.x, d2.y - d3.y)),
                        (int) (d2.y + Math.hypot(d2.x - b2.x, d2.y - b2.y)));
                shadowRotateDegrees = 270.0f - (float) Math.toDegrees(Math.atan2(d2.x - d3.x, d2.y - d3.y));
            } else {
                topPageBackShadowDrawable.setBounds((int) d2.x,
                        (int) (d2.y - Math.hypot(d2.x - b2.x, d2.y - b2.y)),
                        (int) (d2.x + Math.hypot(d2.x - d3.x, d2.y - d3.y)),
                        (int) (d2.y + Math.hypot(d2.x - f2.x, d2.y - f2.y)));
                shadowRotateDegrees = 270.0f - (float) Math.toDegrees(Math.atan2(d2.x - d3.x, d2.y - d3.y));
            }
            //
            // Draw top page back
            //
            path2.reset();
            path2.moveTo(b2.x, b2.y);
            path2.lineTo(c.x, c.y);
            path2.lineTo(m1.x, m1.y);
            path2.lineTo(e.x, e.y);
            path2.lineTo(f2.x, f2.y);
            path2.close();
            canvas.save();
            canvas.clipPath(path1, Region.Op.INTERSECT);
            canvas.clipPath(path2, Region.Op.INTERSECT);
            canvas.drawBitmap(topPageBitmap, topPageBackMatrix, topPageBackPaint);
            canvas.rotate(shadowRotateDegrees, d2.x, d2.y);
            topPageBackShadowDrawable.draw(canvas);
            canvas.restore();
        }

        private void drawBottomPage(Canvas canvas, Bitmap bottomPageBitmap) {
            //
            // Calculate shadow bounds and rotate degrees
            //
            float shadowRotateDegrees;
            if (n.y == 0.0f) {
                bottomPageShadowDrawable.setBounds((int) d3.x,
                        (int) (d3.y - Math.hypot(d3.x - a.x, d3.y - a.y)),
                        (int) (d3.x + Math.hypot(d3.x - d1.x, d3.y - d1.y)),
                        (int) (d3.y + Math.hypot(d3.x - g.x, d3.y - g.y)));
                shadowRotateDegrees = 90.0f - (float) Math.toDegrees(Math.atan2(d1.x - d3.x, d1.y - d3.y));
            } else {
                bottomPageShadowDrawable.setBounds((int) d3.x,
                        (int) (d3.y - Math.hypot(d3.x - g.x, d3.y - g.y)),
                        (int) (d3.x + Math.hypot(d3.x - d1.x, d3.y - d1.y)),
                        (int) (d3.y + Math.hypot(d3.x - a.x, d3.y - a.y)));
                shadowRotateDegrees = 90.0f - (float) Math.toDegrees(Math.atan2(d1.x - d3.x, d1.y - d3.y));
            }
            //
            // Draw bottom page
            //
            path2.reset();
            path2.moveTo(n.x, n.y);
            path2.lineTo(a.x, a.y);
            path2.lineTo(b2.x, b2.y);
            path2.lineTo(f2.x, f2.y);
            path2.lineTo(g.x, g.y);
            path2.lineTo(n.x, canvas.getHeight() - n.y);
            path2.lineTo(canvas.getWidth(), canvas.getHeight() - n.y);
            path2.lineTo(canvas.getWidth(), n.y);
            path2.close();
            canvas.save();
            canvas.clipPath(path1, Region.Op.INTERSECT);
            canvas.clipPath(path2, Region.Op.INTERSECT);
            canvas.drawBitmap(bottomPageBitmap, 0.0f, 0.0f, null);
            canvas.rotate(shadowRotateDegrees, d3.x, d3.y);
            bottomPageShadowDrawable.draw(canvas);
            canvas.restore();
        }

        private void drawDebugElements(Canvas canvas) {
            float density = Resources.getSystem().getDisplayMetrics().density;
            float unit = 2.0f * density;
            Paint paint = new Paint();
            paint.setColor(0xffff0000);
            //
            // Draw lines.
            //
            paint.setStrokeWidth(1.0f);
            canvas.drawLine(m1.x, m1.y, n.x, n.y, paint);
            canvas.drawLine(m1.x, m1.y, c.x, c.y, paint);
            canvas.drawLine(m1.x, m1.y, e.x, e.y, paint);
            canvas.drawLine(b3.x, b3.y, b1.x, b1.y, paint);
            canvas.drawLine(f3.x, f3.y, f1.x, f1.y, paint);
            canvas.drawLine(a.x, a.y, g.x, g.y, paint);
            canvas.drawLine(b1.x, b1.y, f1.x, f1.y, paint);
            canvas.drawLine(b2.x, b2.y, f2.x, f2.y, paint);
            //
            // Draw points.
            //
            paint.setStrokeWidth(unit);
            canvas.drawPoint(m1.x, m1.y, paint);
            canvas.drawPoint(m2.x, m2.y, paint);
            canvas.drawPoint(a.x, a.y, paint);
            canvas.drawPoint(b1.x, b1.y, paint);
            canvas.drawPoint(b2.x, b2.y, paint);
            canvas.drawPoint(b3.x, b3.y, paint);
            canvas.drawPoint(c.x, c.y, paint);
            canvas.drawPoint(d1.x, d1.y, paint);
            canvas.drawPoint(d2.x, d2.y, paint);
            canvas.drawPoint(d3.x, d3.y, paint);
            canvas.drawPoint(e.x, e.y, paint);
            canvas.drawPoint(f1.x, f1.y, paint);
            canvas.drawPoint(f2.x, f2.y, paint);
            canvas.drawPoint(f3.x, f3.y, paint);
            canvas.drawPoint(g.x, g.y, paint);
            canvas.drawPoint(n.x, n.y, paint);
            //
            // Draw texts.
            //
            paint.setTextSize(12.0f * density);
            paint.setFakeBoldText(true);
            if (n.y == canvas.getHeight()) {
                canvas.drawText("m1", m1.x + unit, m1.y - unit, paint);
                canvas.drawText("m2", m2.x - 10.0f * unit, m2.y - 2.0f * unit, paint);
                canvas.drawText("a", a.x + unit, a.y - unit, paint);
                canvas.drawText("b1", b1.x + unit, b1.y - unit, paint);
                canvas.drawText("b2", b2.x + unit, b2.y - unit, paint);
                canvas.drawText("b3", b3.x + unit, b3.y - unit, paint);
            } else {
                canvas.drawText("m1", m1.x + unit, m1.y + 5.0f * unit, paint);
                canvas.drawText("m2", m2.x - 10.0f * unit, m2.y + 5.0f * unit, paint);
                canvas.drawText("a", a.x + unit, a.y + 5.0f * unit, paint);
                canvas.drawText("b1", b1.x + unit, b1.y + 5.0f * unit, paint);
                canvas.drawText("b2", b2.x + unit, b2.y + 5.0f * unit, paint);
                canvas.drawText("b3", b3.x + unit, b3.y + 5.0f * unit, paint);
            }
            canvas.drawText("c", c.x + unit, c.y - unit, paint);
            canvas.drawText("d1", d1.x + unit, d1.y - unit, paint);
            canvas.drawText("d2", d2.x + unit, d2.y - unit, paint);
            canvas.drawText("d3", d3.x + unit, d3.y - unit, paint);
            canvas.drawText("e", e.x + unit, e.y - unit, paint);
            canvas.drawText("f1", f1.x - 7.0f * unit, f1.y - unit, paint);
            canvas.drawText("f2", f2.x - 7.0f * unit, f2.y - unit, paint);
            canvas.drawText("f3", f3.x - 7.0f * unit, f3.y - unit, paint);
            canvas.drawText("g", g.x - 5.0f * unit, g.y - unit, paint);
            if (n.y == canvas.getHeight()) {
                canvas.drawText("n", n.x - 5.0f * unit, n.y - unit, paint);
            } else {
                canvas.drawText("n", n.x - 5.0f * unit, n.y + 5.0f * unit, paint);
            }
        }
    }

    private static class CoverPageTurningAnimator {
        private final int shadowWidth = (int) (6 * Resources.getSystem().getDisplayMetrics().density);
        private final GradientDrawable shadowDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {0x00000000, 0x00000000});

        private CoverPageTurningAnimator() {
        }

        void setColorSetting(ReaderColorSetting colorSetting) {
            if (colorSetting.isLightBackground) {
                shadowDrawable.setColors(new int[] {0x66333333, 0x00000000});
            } else {
                shadowDrawable.setColors(new int[] {0x66111111, 0x00000000});
            }
        }

        void draw(Canvas canvas, Bitmap bottomPageBitmap, Bitmap topPageBitmap,
                  PointF actionDownPoint, PointF actionMovePoint, float actionDeltaX) {
            canvas.drawBitmap(bottomPageBitmap, 0.0f, 0.0f, null);
            canvas.save();
            if (actionDeltaX < 0.0f) {
                canvas.translate(actionDeltaX, 0.0f);
            } else {
                canvas.translate(actionDownPoint.x + actionDeltaX - canvas.getWidth(), 0.0f);
            }
            canvas.drawBitmap(topPageBitmap, 0.0f, 0.0f, null);
            shadowDrawable.setBounds(canvas.getWidth(),
                    0,
                    canvas.getWidth() + shadowWidth,
                    canvas.getHeight());
            shadowDrawable.draw(canvas);
            canvas.restore();
        }

        void startScroll(Scroller scroller, int width, int height,
                         PointF actionDownPoint, PointF actionMovePoint, int actionDirection) {
            int startX = (int) actionMovePoint.x;
            int distanceX = (actionDirection < 0) ? -startX : (width - startX);
            scroller.startScroll(startX, 0, distanceX, 0);
        }
    }
}
