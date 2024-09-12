package com.example.myapplication;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class test extends AppCompatActivity {

    private ImageView joystick;
    private Button upButton, downButton, leftButton, rightButton;
    private float dX, dY;
    private float initialX, initialY; // Vị trí ban đầu của joystick

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        joystick = findViewById(R.id.joystick);
        upButton = findViewById(R.id.upButton);
        downButton = findViewById(R.id.downButton);
        leftButton = findViewById(R.id.leftButton);
        rightButton = findViewById(R.id.rightButton);

        // Lưu vị trí ban đầu của joystick
        joystick.post(() -> {
            initialX = joystick.getX();
            initialY = joystick.getY();
        });

        joystick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Lưu trữ vị trí ban đầu khi chạm
                        dX = view.getX() - motionEvent.getRawX();
                        dY = view.getY() - motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // Cập nhật vị trí khi di chuyển
                        view.animate()
                                .x(motionEvent.getRawX() + dX)
                                .y(motionEvent.getRawY() + dY)
                                .setDuration(0)
                                .start();

                        // Kiểm tra va chạm với các nút
                        checkCollisionWithButtons(view);
                        break;
                    case MotionEvent.ACTION_UP:
                        // Đưa joystick về vị trí trung tâm khi nhả ra
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
        Toast.makeText(this, "Up", Toast.LENGTH_SHORT).show();

    }

    private void down() {
        Toast.makeText(this, "Down", Toast.LENGTH_SHORT).show();
    }

    private void left() {
        Toast.makeText(this, "Left", Toast.LENGTH_SHORT).show();
    }

    private void right() {
        Toast.makeText(this, "Right", Toast.LENGTH_SHORT).show();
    }
}
