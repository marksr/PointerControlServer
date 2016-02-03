package br.com.marksr.pointercontrol;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;

import com.marksr.pointercontrol.R;
import br.com.marksr.pointercontrol.commons.PointerCommand;
import br.com.marksr.pointercontrol.commons.PointerCommandAdapter;

public class PointerServer extends Service {
    private static final String TAG = PointerServer.class.getSimpleName();
    private WindowManager windowManager;
    private ImageView chatHead;
    private WindowManager.LayoutParams params;
    private CommandsServer commandsServer;

    public PointerServer() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        chatHead = new ImageView(this);
        chatHead.setImageResource(R.drawable.cursor_icon_filled);


        params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
//        WindowManager.LayoutParams.TYPE_PHONE,
//        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
//        WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
        WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_FULLSCREEN |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT);

        params.width = 30*2;
        params.height = 30*2;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        final PointerCommand orientationChanged = new PointerCommandAdapter() {

            @Override
            public String execute() {
                return super.execute();
            }
        };

        final OrientationEventListener orientationListener =
                new OrientationEventListener(getApplicationContext(), SensorManager.SENSOR_DELAY_NORMAL) {

            private final Integer dicreteAngles[] = {360, 0, 90, 180, 270};
            @Override
            public void onOrientationChanged(int orientation) {


                Log.d(TAG, String.valueOf(orientation));
            }
        };

        orientationListener.enable();

        final PointerCommand screeenResolution = new PointerCommandAdapter() {

            @Override
            public String execute() {
                Point p = new Point();
                if (Build.VERSION.SDK_INT >= 17) {
                    windowManager.getDefaultDisplay().getRealSize(p);
                } else {
                    Display d = windowManager.getDefaultDisplay();
                    p.set(d.getWidth(), d.getHeight());
                }
                return "s," + p.x + "," + p.y + "\n";
            }

        };
        commandsServer = new CommandsServer(6789);

        commandsServer.registerCommand(new PointerCommandAdapter('p') {
            @Override
            public void process(String m) {
                short x = (short) ((m.charAt(1) << 8) | m.charAt(2));
                short y = (short) ((m.charAt(3) << 8) | m.charAt(4));
                Log.d(PointerServer.class.getSimpleName(), m);
                Log.d(PointerServer.class.getSimpleName(), "x: "+x+", y: "+y);
            }
        });

        commandsServer.registerCommand(new PointerCommandAdapter('c') {
            @Override
            public void process(String message) {
                String values[] = message.split(",");
                Log.d(PointerServer.class.getSimpleName(), message);

                if (values.length == 3){
                    int x = Integer.parseInt(values[1]);
                    int y = Integer.parseInt(values[2]);
                    params.x = x;
                    params.y = y;
                    windowManager.updateViewLayout(chatHead, params);
                }
            }
        });

        commandsServer.registerCommand(new PointerCommandAdapter('s') {
            @Override
            public void process(String message) {
                commandsServer.sendCommand(screeenResolution);
            }
        });

        commandsServer.start();

        //this code is for dragging the chat head
        chatHead.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX
                                + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY
                                + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(chatHead, params);
                        return true;
                }
                return false;
            }
        });
        windowManager.addView(chatHead, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatHead != null)
            windowManager.removeView(chatHead);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
