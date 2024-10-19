package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.PendingIntentCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.devadvance.circularseekbar.CircularSeekBar;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
    //PrintScreen
    private FrameLayout frmPrintScreen;
    //
    private ImageView upButton, downButton, leftButton, rightButton;
    private Button btnLeftClickMouse,btnRightClickMouse;
    private ImageView btnReturnDetailView;
    //speaker
    private TextView txtValueofSpeaker;
    private CircularSeekBar circularSeekBar1;
    private Switch swtSpeaker;
    //rotate
    private ImageView btnRotate;
    private boolean isLandscape = false;


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
        btnRotate = findViewById(R.id.btnRotate);
        frmPrintScreen = findViewById(R.id.frmPrintScreen);

        mouseControlView = getLayoutInflater().inflate(R.layout.control_mouse, null);
        speakerControlView = getLayoutInflater().inflate(R.layout.control_speaker,null);

        screenUpdateReceiver = new ScreenUpdateReceiver();
        IntentFilter filter = new IntentFilter("UPDATE_SCREEN_IMAGE");
        registerReceiver(screenUpdateReceiver, filter);

        //getVl
        txtValueofSpeaker = speakerControlView.findViewById(R.id.txtValueofSpeaker);
        circularSeekBar1  = speakerControlView.findViewById(R.id.circularSeekBar1);
        swtSpeaker = speakerControlView.findViewById(R.id.swtSpeaker);
        btnReturnDetailView.setOnClickListener(v -> {
            finish();
        });

        btnRotate.setOnClickListener(v -> {
            if (isLandscape) {
                // Trở lại trạng thái ban đầu - dọc
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

                // Khôi phục kích thước ban đầu của photoView
                ViewGroup.LayoutParams params = photoView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = 300; // Kích thước ban đầu
                photoView.setLayoutParams(params);

                // Thay đổi kiểu hiển thị
                photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                isLandscape = true;
            } else {
                // Xoay sang trạng thái ngang
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                // Chỉnh kích thước để lấp đầy màn hình
                ViewGroup.LayoutParams params = photoView.getLayoutParams();
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                photoView.setLayoutParams(params);

                // Thay đổi kiểu hiển thị để lấp đầy màn hình ngang
                photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                isLandscape = true;
            }
        });
        btnMouse.setOnClickListener(v->{
            showControlView(mouseControlView);
            showControl.removeView(mouseControlView);
            btnMouse.setImageResource(R.drawable.bntmdrk);
            showControl.removeView(speakerControlView);
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

            upButton.setOnClickListener(view->{
                upButton.setImageResource(R.drawable.clickedmouse);
                new Thread(() -> sendMouseCommand("MOVE_UP")).start();
                handler.postDelayed(() -> upButton.setImageResource(R.drawable.btnumouse), 200);
            });
            downButton.setOnClickListener(view->{
                downButton.setImageResource(R.drawable.clickedownmouse);
                new Thread(() -> sendMouseCommand("MOVE_DOWN")).start();
                handler.postDelayed(() -> downButton.setImageResource(R.drawable.btndmouse), 200);
            });
            leftButton.setOnClickListener(view->{
                leftButton.setImageResource(R.drawable.clickedleftmouse);
                new Thread(() -> sendMouseCommand("MOVE_LEFT")).start();
                handler.postDelayed(() -> leftButton.setImageResource(R.drawable.btnlmouse), 200);
            });
            rightButton.setOnClickListener(view->{
                rightButton.setImageResource(R.drawable.clickedrightmouse);
                new Thread(() -> sendMouseCommand("MOVE_RIGHT")).start();
                handler.postDelayed(() -> rightButton.setImageResource(R.drawable.btnrmouse), 200);
            });

            btnLeftClickMouse.setOnClickListener(view -> {
                new Thread(() ->sendMouseCommand("LEFT_CLICK")).start();
            });
            btnRightClickMouse.setOnClickListener(view -> {
                new Thread(() -> sendMouseCommand("RIGHT_CLICK")).start();
            });

            showControlView(mouseControlView);
            btnMouse.setImageResource(R.drawable.bntmdrk);


        });
        txtQuality.setOnClickListener( v ->{
            Toast.makeText(this," Highest quality",Toast.LENGTH_SHORT).show();
        });
        btnSpeaker.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showControlView(speakerControlView);
                btnSpeaker.setImageResource(R.drawable.btnsdrk);
                btnMouse.setImageResource(R.drawable.btnmgray);
                // Start a new thread to handle network communication
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket("192.168.1.26", 12345);
                            // Read the message from the server
                            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            final String message = input.readLine();
                            // Close the socket
                            socket.close();
                            int test = Integer.parseInt(message);
                            String value = message.toString() +"%";
                            // Update the UI on the main thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtValueofSpeaker.setText(value);
                                    circularSeekBar1.setProgress(test);
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

        });
        frmPrintScreen.setOnClickListener(v ->{
            saveScreenshot();
        });
        circularSeekBar1.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                String tmp = String.valueOf(progress) +"%";
                txtValueofSpeaker.setText(tmp);
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {

            }
        });
        swtSpeaker.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Tạo luồng mới để xử lý việc gửi yêu cầu tới server Python
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Kết nối tới server Python
                            Socket socket = new Socket("192.168.1.26", 11111);
                            OutputStream outputStream = socket.getOutputStream();
                            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);

                            // Kiểm tra trạng thái của Switch để gửi lệnh ON hoặc OFF
                            if (isChecked) {
                                // Switch bật, gửi lệnh "ON"
                                writer.println("ON");
                            } else {
                                // Switch tắt, gửi lệnh "OFF"
                                writer.println("OFF");
                            }

                            // Đọc phản hồi từ server
                            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            final String response = input.readLine();

                            // Đóng kết nối socket
                            socket.close();

                            // Cập nhật giao diện (UI) theo phản hồi nhận được
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(show_detail_viewscr.this, response, Toast.LENGTH_SHORT).show();
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
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

    public void saveScreenshot() {
        // Tạo một Bitmap từ PhotoView
        photoView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(photoView.getDrawingCache());
        photoView.setDrawingCacheEnabled(false);

        // Sử dụng thư mục an toàn hơn trong thư mục Pictures của ứng dụng
        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Screenshots");
        if (!directory.exists()) {
            directory.mkdirs(); // Tạo thư mục nếu chưa tồn tại
        }

        // Tạo file để lưu ảnh chụp màn hình
        File file = new File(directory, "screenshot_" + System.currentTimeMillis() + ".png");
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            boolean saved = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream); // Nén và lưu ảnh
            outputStream.flush();
            outputStream.close();

            // Kiểm tra nếu việc lưu thành công
            if (saved) {
                Log.e("Test","\"Screenshot saved successfully to:"+file.getAbsolutePath());
                Toast.makeText(this, "Screenshot saved successfully to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to save screenshot!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Test",e.toString());
            Toast.makeText(this, "Error occurred while saving screenshot: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }






}