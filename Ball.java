package pl.jawegiel.dribbler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class Ball extends View {
    Paint p;
    private Bitmap ball;
    float x = 0;
    float y = 0;
    float oldX;
    float oldY;
    float wysAkt = 1f;
    float wysMax = 1f;
    boolean rosnie;
    boolean stop;
    Path path;
    int pkt;
    float tapX;
    float tapY;
    float odl;
    int width;
    int height;
    boolean odbL;
    boolean odbP;
    boolean odbD;
    boolean odbG;
    Dribbler pod;
    public float speed = 0.0125f;
    public void setSpeed(float speed) {
        this.speed = speed;
    }
    float wspSpeed = 1;
    boolean bezp=false;
    float v = 1.0f;
    float v2 = 1.05f;

    public void setV2(float v2) {
        this.v2 = v2;
    }


    public float getWysAkt() {
        return wysAkt;
    }

    public Ball(Context context) {
        super(context);
    }

    public Ball(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        p = new Paint();
        p.setColor(Color.MAGENTA);
        p.setTextSize(20);

        width = size.x;
        height = size.y;

        if(width==720) ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
        if(width<720) ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball_sm);
        if(width>720) ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball_la);


        pod = (Dribbler)getActivity();
        path = new Path();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        x = getX();
        y = getY();

        canvas.drawBitmap(ball, 0, 0, p);
        canvas.drawText(String.valueOf(wysAkt), x,y+150,p);

        kolizjaSciany();

        setX(x);
        setY(y);


        invalidate();
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        if(width==720) super.onMeasure(View.MeasureSpec.makeMeasureSpec(220, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(220, View.MeasureSpec.EXACTLY));
        if(width<720) super.onMeasure(View.MeasureSpec.makeMeasureSpec(150, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(150, View.MeasureSpec.EXACTLY));
        if(width>720) super.onMeasure(View.MeasureSpec.makeMeasureSpec(330, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(330, View.MeasureSpec.EXACTLY));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                invalidate();
                break;
        }
        return true;
    }





    public Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }


    public void podbijanie() {
         new Thread( ()->
         {
                while (!stop) {
                    try {
                        ((Dribbler) getContext()).runOnUiThread( () -> {

                                if (wysMax <= 0.3f && !stop) { koniecGry();  stop = true; }


                                if(width==720)
                                {
                                    setScaleX(wysAkt);
                                    setScaleY(wysAkt);
                                }

                                if(width<720)
                                {
                                    setScaleX(wysAkt*(150/220));
                                    setScaleY(wysAkt*(150/220));
                                }

                                if(width>720)
                                {
                                    setScaleX(wysAkt*(330/220));
                                    setScaleY(wysAkt*(330/220));
                                }

                        });
                        Thread.sleep(30);


                        if (!rosnie && !stop)
                        {
                            v=v*v2;


                            wysAkt = wysAkt - (speed * v);

                            pod.reka.zmGraw = true;
                            if (wysAkt <= 0.3f && !bezp) { pkt++;  bezp=true; }
                            if(wysAkt <= 0.3f && bezp) rosnie = true;
                        }
                        if (rosnie && !stop)
                        {
                            //sound=true;
                            if(wysAkt>0.3) bezp=false;

                            // im większa wysAkt tym wolniej rośnie aż do zera

                            // speed = 0.0125f, v = 1.0f, v2 = 1.05f
                            v=v/v2;
                            if(v<1f) v=1f;
                            wysAkt = wysAkt + (speed * v);



                            if (wysAkt >= wysMax) { tracWysokoscNaturalnie(); rosnie = false; }

                            if (pod.reka.isZmTap()) { tracWysokoscTapniecie(); rosnie = false; }

                            if (!pod.reka.zmGraw && !pod.reka.isZmTap()) { rosnie = false; }
                        }

                        if (stop) return;
                    } catch (InterruptedException e) { e.printStackTrace(); }
                }


        }).start();
    }

    // ściany i ręka
    private void kolizjaSciany()
    {
        if(width==720) {
            if (x + 220 < width && !odbP) x = x + (odl * 0.05f * wspSpeed);
            if (x + 200 >= width) {
                odbP = true;
                odbL = false;
            }
            if (odbP) x = x - (odl * 0.05f * wspSpeed);
            if (x > 0 && !odbL) x = x - (odl * 0.05f * wspSpeed);
            if (x <= 0) {
                odbL = true;
                odbP = false;
            }
            if (odbL) x = x + (odl * 0.05f * wspSpeed);

            if (y + 220 + 130 < height && !odbD) y = y + (odl * 0.05f * wspSpeed);
            if (y + 220 + 130 >= height) {
                odbD = true;
                odbG = false;
            }
            if (odbD) y = y - (odl * 0.05f * wspSpeed);
            if (y > 0 && !odbG) y = y - (odl * 0.05f * wspSpeed);
            if (y <= 0) {
                odbG = true;
                odbD = false;
            }
            if (odbG) y = y + (odl * 0.05f * wspSpeed);
        }
        if(width<720) {
            if (x + 150 < width && !odbP) x = x + (odl * 0.05f * wspSpeed);
            if (x + 150 >= width) {
                odbP = true;
                odbL = false;
            }
            if (odbP) x = x - (odl * 0.05f * wspSpeed);
            if (x > 0 && !odbL) x = x - (odl * 0.05f * wspSpeed);
            if (x <= 0) {
                odbL = true;
                odbP = false;
            }
            if (odbL) x = x + (odl * 0.05f * wspSpeed);

            if (y + 150 + 130 < height && !odbD) y = y + (odl * 0.05f * wspSpeed);
            if (y + 150 + 130 >= height) {
                odbD = true;
                odbG = false;
            }
            if (odbD) y = y - (odl * 0.05f * wspSpeed);
            if (y > 0 && !odbG) y = y - (odl * 0.05f * wspSpeed);
            if (y <= 0) {
                odbG = true;
                odbD = false;
            }
            if (odbG) y = y + (odl * 0.05f * wspSpeed);
        }
        if(width>720) {
            if (x + 330 < width && !odbP) x = x + (odl * 0.05f * wspSpeed);
            if (x + 330 >= width) {
                odbP = true;
                odbL = false;
            }
            if (odbP) x = x - (odl * 0.05f * wspSpeed);
            if (x > 0 && !odbL) x = x - (odl * 0.05f * wspSpeed);
            if (x <= 0) {
                odbL = true;
                odbP = false;
            }
            if (odbL) x = x + (odl * 0.05f * wspSpeed);

            if (y + 330 + 130 < height && !odbD) y = y + (odl * 0.05f * wspSpeed);
            if (y + 330 + 130 >= height) {
                odbD = true;
                odbG = false;
            }
            if (odbD) y = y - (odl * 0.05f * wspSpeed);
            if (y > 0 && !odbG) y = y - (odl * 0.05f * wspSpeed);
            if (y <= 0) {
                odbG = true;
                odbD = false;
            }
            if (odbG) y = y + (odl * 0.05f * wspSpeed);
        }
    }











    // kierunek po naciśnięciu
    public void wyznaczKierunek2(float tapX, float tapY, float odl)
    {
        this.tapX = tapX; //tapnięcie X
        this.tapY = tapY; //tapniecie Y
        this.odl = odl;
        oldX = getX(); // lewa gora pilki X
        oldY = getY(); // lewa gora pilki Y

        if(oldX+110>tapX) { odbP = false; odbL = true; }
        if(oldX+110<tapX) { odbL = false; odbP = true; }
        if(oldY+110>tapY) { odbD = false; odbG = true; }
        if(oldY+110<tapY) { odbG = false; odbD = true; }

        invalidate();
    }














    private void tracWysokoscNaturalnie() { wysMax = wysMax-(float)0.1; }
    private void tracWysokoscTapniecie() { wysMax = wysAkt; }

    private void koniecGry()
    {
        BazaRekordy bd = new BazaRekordy(getContext());
        bd.insertRecord(String.valueOf(pkt), pod.diff);
        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Game over!");
        alertDialog.setMessage("Your result: "+String.valueOf(pkt));
        alertDialog.setIcon(R.drawable.tlo);
        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "OK", (DialogInterface d, int which) -> getActivity().startActivity(new Intent(getActivity(), IntersitialActivity.class)));
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
}
