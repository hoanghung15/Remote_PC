package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.PendingIntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.OutputStream;

public class show_detail_viewscr extends AppCompatActivity {
    private PhotoView photoView;
    private ScreenUpdateReceiver screenUpdateReceiver;
    private ImageView btnMouse;
    private FrameLayout showControl;
    private View mouseControlView;
    private View speakerControlView;

    private FrameLayout joystick;
    private float initialX, initialY;
    private float dX, dY;

    private ImageView btnKeyBoardGray,btnSpeaker;
    private TextView txtQuality;

    private Handler handler = new Handler(Looper.getMainLooper());

    private boolean firstView = true;

    private ImageView upButton, downButton, leftButton, rightButton;
    private Button btnLeftClickMouse,btnRightClickMouse;
    private ImageView btnReturnDetailView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_show_detail_viewscr);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        photoView = findViewById(R.id.photoView);
        btnMouse = findViewById(R.id.btnMouse);
        showControl = findViewById(R.id.showControl);
        btnReturnDetailView = findViewById(R.id.btnReturnDetailView);
        btnKeyBoardGray = findViewById(R.id.btnKeyBoardGray);
        txtQuality = findViewById(R.id.txtQuality);
        btnSpeaker = findViewById(R.id.btnSpeaker);

        mouseControlView = getLayoutInflater().inflate(R.layout.control_mouse, null);
        speakerControlView = getLayoutInflater().inflate(R.layout.control_speaker,null);

        screenUpdateReceiver = new ScreenUpdateReceiver();
        IntentFilter filter = new IntentFilter("UPDATE_SCREEN_IMAGE");
        registerReceiver(screenUpdateReceiver, filter);

        btnReturnDetailView.setOnClickListener(v -> {
            finish();
        });
        txtQuality.setOnClickListener( v ->{
            Toast.makeText(this," Highest quality",Toast.LENGTH_SHORT).show();
        });

        if (firstView) {
            // Khởi tạo joystick và các nút điều khiển chỉ khi firstView là true
            joystick = mouseControlView.findViewById(R.id.joystick);
            upButton = mouseControlView.findViewById(R.id.btnUpmouse);
            downButton = mouseControlView.findViewById(R.id.btnDownMouse);
            leftButton = mouseControlView.findViewById(R.id.btnLeftMouse);
            rightButton = mouseControlView.findViewById(R.id.btnRightMouse);
            btnLeftClickMouse = mouseControlView.findViewById(R.id.btnLeftClickMouse);
            btnRightClickMouse = mouseControlView.findViewById(R.id.btnRightClickMouse);

            // Lưu vị trí ban đầu của joystick
            joystick.post(() -> {
                initialX = joystick.getX();
                initialY = joystick.getY();
            });

            joystick.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    // Lấy tọa độ của frmMoveMouse để giới hạn joystick
                    FrameLayout frmMoveMouse = findViewById(R.id.frmMoveMouse);
                    int parentWidth = frmMoveMouse.getWidth();
                    int parentHeight = frmMoveMouse.getHeight();
                    int joystickWidth = joystick.getWidth();
                    int joystickHeight = joystick.getHeight();

                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            dX = view.getX() - motionEvent.getRawX();
                            dY = view.getY() - motionEvent.getRawY();
                            break;

                        case MotionEvent.ACTION_MOVE:
                            float newX = motionEvent.getRawX() + dX;
                            float newY = motionEvent.getRawY() + dY;

                            // Giới hạn X không vượt quá biên trái và phải
                            if (newX < 0) {
                                newX = 0;
                            } else if (newX + joystickWidth > parentWidth) {
                                newX = parentWidth - joystickWidth;
                            }

                            // Giới hạn Y không vượt quá biên trên và dưới
                            if (newY < 0) {
                                newY = 0;
                            } else if (newY + joystickHeight > parentHeight) {
                                newY = parentHeight - joystickHeight;
                            }

                            view.animate()
                                    .x(newX)
                                    .y(newY)
                                    .setDuration(0)
                                    .start();

                            checkCollisionWithButtons(view);
                            break;

                        case MotionEvent.ACTION_UP:
                            view.animate()
                                    .x(initialX)
                                    .y(initialY)
                                    .setDuration(500)
                                    .start();
                            break;

                        default:
                            return false;
                    }
                    return true;
                }
            });

            upButton.setOnClickListener(v->{
                upButton.setImageResource(R.drawable.clickedmouse);
                new Thread(() -> sendMouseCommand("MOVE_UP")).start();
                handler.postDelayed(() -> upButton.setImageResource(R.drawable.btnumouse), 200);
            });
            downButton.setOnClickListener(v->{
                downButton.setImageResource(R.drawable.clickedownmouse);
                new Thread(() -> sendMouseCommand("MOVE_DOWN")).start();
                handler.postDelayed(() -> downButton.setImageResource(R.drawable.btndmouse), 200);
            });
            leftButton.setOnClickListener(v->{
                leftButton.setImageResource(R.drawable.clickedleftmouse);
                new Thread(() -> sendMouseCommand("MOVE_LEFT")).start();
                handler.postDelayed(() -> leftButton.setImageResource(R.drawable.btnlmouse), 200);
            });
            rightButton.setOnClickListener(v->{
                rightButton.setImageResource(R.drawable.clickedrightmouse);
                new Thread(() -> sendMouseCommand("MOVE_RIGHT")).start();
                handler.postDelayed(() -> rightButton.setImageResource(R.drawable.btnrmouse), 200);
            });

            btnLeftClickMouse.setOnClickListener(v -> {
                new Thread(() ->sendMouseCommand("LEFT_CLICK")).start();
            });
            btnRightClickMouse.setOnClickListener(v -> {
                new Thread(() -> sendMouseCommand("RIGHT_CLICK")).start();
            });

            showControlView(mouseControlView);
            btnMouse.setImageResource(R.drawable.bntmdrk);
            firstView = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(screenUpdateReceiver); // Hủy đăng ký Receiver khi Activity bị hủy
    }

    private class ScreenUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] byteArray = intent.getByteArrayExtra("bitmap");
            if (byteArray != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                photoView.setImageBitmap(bitmap);
            }
        }
    }

    private void showControlView(View controlView) {
        showControl.removeAllViews();  // Xóa tất cả các View cũ trong FrameLayout
        showControl.addView(controlView);  // Thêm View điều khiển mới
    }

    // Hàm kiểm tra va chạm giữa joystick và các nút
    private void checkCollisionWithButtons(View joystick) {
        if (isViewOverlapping(joystick, upButton)) {
            up();
        } else if (isViewOverlapping(joystick, downButton)) {
            down();
        } else if (isViewOverlapping(joystick, leftButton)) {
            left();
        } else if (isViewOverlapping(joystick, rightButton)) {
            right();
        }
    }

    // Hàm kiểm tra nếu hai view chồng lên nhau
    private boolean isViewOverlapping(View firstView, View secondView) {
        Rect firstRect = new Rect();
        Rect secondRect = new Rect();

        firstView.getHitRect(firstRect);
        secondView.getHitRect(secondRect);

        return Rect.intersects(firstRect, secondRect);
    }

    // Các hàm điều khiển
    private void up() {
        upButton.setImageResource(R.drawable.clickedmouse);
        new Thread(() -> sendMouseCommand("MOVE_UP")).start();
        handler.postDelayed(() -> upButton.setImageResource(R.drawable.btnumouse), 500);
    }

    private void down() {
        downButton.setImageResource(R.drawable.clickedownmouse);
        new Thread(() -> sendMouseCommand("MOVE_DOWN")).start();
        handler.postDelayed(() -> downButton.setImageResource(R.drawable.btndmouse), 500);
    }

    private void left() {
        leftButton.setImageResource(R.drawable.clickedleftmouse);
        new Thread(() -> sendMouseCommand("MOVE_LEFT")).start();
        handler.postDelayed(() -> leftButton.setImageResource(R.drawable.btnlmouse), 500);
    }

    private void right() {
        rightButton.setImageResource(R.drawable.clickedrightmouse);
        new Thread(() -> sendMouseCommand("MOVE_RIGHT")).start();
        handler.postDelayed(() -> rightButton.setImageResource(R.drawable.btnrmouse), 500);
    }

    private void sendMouseCommand(String command){
        try{
            OutputStream outputStream = SocketManager.getOutputStream();
            if(outputStream != null){
                outputStream.write(command.getBytes());
                outputStream.flush();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
