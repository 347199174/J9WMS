

package com.google.zxing.client.android;

import com.example.admin.j9wms.IndexActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.camera.CameraManager;

import com.google.zxing.client.android.history.HistoryActivity;
import com.google.zxing.client.android.history.HistoryItem;
import com.google.zxing.client.android.history.HistoryManager;
import com.google.zxing.client.android.result.ResultButtonListener;
import com.google.zxing.client.android.result.ResultHandler;
import com.google.zxing.client.android.result.ResultHandlerFactory;
import com.google.zxing.client.android.result.supplement.SupplementalInfoRetriever;
import com.example.admin.j9wms.R;
import com.google.zxing.client.result.ResultParser;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.Canvas;
import android.graphics.Paint;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;


public final class CaptureActivity extends Activity implements SurfaceHolder.Callback {

  private static final String TAG = CaptureActivity.class.getSimpleName();

    private boolean getTrans,getStatus;
  public static final int HISTORY_REQUEST_CODE = 0x0000bacc;
  private CameraManager cameraManager;
  private CaptureActivityHandler handler;
  private Result savedResultToShow;
  private ViewfinderView viewfinderView;
  private TextView statusView,setNameText;
  //private View resultView;
  private Result lastResult;
  private boolean hasSurface;
  private boolean isFlashlightOpen;
  private IntentSource source;
  private String sourceUrl;
  private ScanFromWebPageManager scanFromWebPageManager;
  private Collection<BarcodeFormat> decodeFormats;
  private Map<DecodeHintType,?> decodeHints;
  private String characterSet;
  private HistoryManager historyManager;
  private InactivityTimer inactivityTimer;
  private BeepManager beepManager;
  private AmbientLightManager ambientLightManager;

  ViewfinderView getViewfinderView() {
    return viewfinderView;
  }

  public Handler getHandler() {
    return handler;
  }

  CameraManager getCameraManager() {
    return cameraManager;
  }

  @Override
  public void onCreate(Bundle icicle) {



        super.onCreate(icicle);
           //让屏幕保持恒亮
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.capture);

        hasSurface = false;
      //InactiveTimer是负责：如果长时间没有操作，此APP会自动退出 ，默认时间是5分钟。
        inactivityTimer = new InactivityTimer(this);
      // BeepManager是负责：在二维码解码成功时 播放“bee”的声音，同时还可以震动。
        beepManager = new BeepManager(this);
        ambientLightManager = new AmbientLightManager(this);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
      final Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
      findViewById(R.id.capture_flashlight).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              if (isFlashlightOpen) {
                  cameraManager.setTorch(false); // 关闭闪光灯
                  isFlashlightOpen = false;
              } else {
                  cameraManager.setTorch(true); // 打开闪光灯
                  isFlashlightOpen = true;
              }
          }
      });
      findViewById(R.id.capture_history).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {

              intent.setClassName(CaptureActivity.this, HistoryActivity.class.getName());
              startActivityForResult(intent, HISTORY_REQUEST_CODE);
          }
      });
      findViewById(R.id.capture_set).setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              intent.setClassName(CaptureActivity.this, PreferencesActivity.class.getName());
              startActivity(intent);
          }
      });

      }

      @Override
      protected void onResume() {
        super.onResume();

        // historyManager must be initialized here to update the history preference
        historyManager = new HistoryManager(this);
        historyManager.trimHistory();

        // CameraManager must be initialized here, not in onCreate(). This is necessary because we don't
        // want to open the camera driver and measure the screen size if we're going to show the help on
        // first launch. That led to bugs where the scanning rectangle was the wrong size and partially
        // off screen.
        cameraManager = new CameraManager(getApplication());

        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
        viewfinderView.setCameraManager(cameraManager);

        //resultView = findViewById(R.id.result_view);
        statusView = (TextView) findViewById(R.id.status_view);

        handler = null;
        lastResult = null;


        resetStatusView();


        beepManager.updatePrefs();
        ambientLightManager.start(cameraManager);

        inactivityTimer.onResume();

          setNameText = (TextView)findViewById(R.id.capture_top_hint);
          //接收传递的参数
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        getTrans = bundle.getBoolean("isStore");
          if (getTrans)
          {
              setNameText.setText("入库扫描");
          }
          else
          {
              setNameText.setText("出库扫描");
          }
         getStatus = bundle.getBoolean("isCargo");
          if (getStatus)
          {
              statusView.setText("请扫描仓品的条码");
          }
          else statusView.setText("请扫描仓位码");

        source = IntentSource.NONE;
        sourceUrl = null;
        scanFromWebPageManager = null;
        decodeFormats = null;
        characterSet = null;

        if (intent != null) {

          String action = intent.getAction();
          String dataString = intent.getDataString();

          if (Intents.Scan.ACTION.equals(action)) {

            // Scan the formats the intent requested, and return the result to the calling activity.
            source = IntentSource.NATIVE_APP_INTENT;
            decodeFormats = DecodeFormatManager.parseDecodeFormats(intent);
            decodeHints = DecodeHintManager.parseDecodeHints(intent);

            if (intent.hasExtra(Intents.Scan.WIDTH) && intent.hasExtra(Intents.Scan.HEIGHT)) {
              int width = intent.getIntExtra(Intents.Scan.WIDTH, 0);
              int height = intent.getIntExtra(Intents.Scan.HEIGHT, 0);
              if (width > 0 && height > 0) {
                cameraManager.setManualFramingRect(width, height);
              }
            }

            if (intent.hasExtra(Intents.Scan.CAMERA_ID)) {
              int cameraId = intent.getIntExtra(Intents.Scan.CAMERA_ID, -1);
              if (cameraId >= 0) {
                cameraManager.setManualCameraId(cameraId);
              }
            }

            String customPromptMessage = intent.getStringExtra(Intents.Scan.PROMPT_MESSAGE);
            if (customPromptMessage != null) {
              statusView.setText(customPromptMessage);
            }

          } else if (dataString != null &&
                     dataString.contains("http://www.google") &&
                     dataString.contains("/m/products/scan")) {

            // Scan only products and send the result to mobile Product Search.
            source = IntentSource.PRODUCT_SEARCH_LINK;
            sourceUrl = dataString;
            decodeFormats = DecodeFormatManager.PRODUCT_FORMATS;

          }

          characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET);

        }

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
          // The activity was paused but not stopped, so the surface still exists. Therefore
          // surfaceCreated() won't be called, so init the camera here.
          initCamera(surfaceHolder);
        } else {
          // Install the callback and wait for surfaceCreated() to init the camera.
          surfaceHolder.addCallback(this);
        }
      }


      @Override
      protected void onPause() {
        if (handler != null) {
          handler.quitSynchronously();
          handler = null;
        }
        inactivityTimer.onPause();
        ambientLightManager.stop();
        beepManager.close();
        cameraManager.closeDriver();
        //historyManager = null; // Keep for onActivityResult
        if (!hasSurface) {
          SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
          SurfaceHolder surfaceHolder = surfaceView.getHolder();
          surfaceHolder.removeCallback(this);
        }
        super.onPause();
      }

      @Override
      protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
      }

      @Override
      public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
          case KeyEvent.KEYCODE_BACK:
            if (source == IntentSource.NATIVE_APP_INTENT) {
              setResult(RESULT_CANCELED);
              finish();
              return true;
            }
            if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK) && lastResult != null) {
              restartPreviewAfterDelay(0L);
              return true;
            }
            break;
          case KeyEvent.KEYCODE_FOCUS:
          case KeyEvent.KEYCODE_CAMERA:
            // Handle these events so they don't launch the Camera app
            return true;
          // Use volume up/down to turn on light
          case KeyEvent.KEYCODE_VOLUME_DOWN:
            cameraManager.setTorch(false);
            return true;
          case KeyEvent.KEYCODE_VOLUME_UP:
            cameraManager.setTorch(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
      }

      @Override
      public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
          if (requestCode == HISTORY_REQUEST_CODE) {
            int itemNumber = intent.getIntExtra(Intents.History.ITEM_NUMBER, -1);
            if (itemNumber >= 0) {
              HistoryItem historyItem = historyManager.buildHistoryItem(itemNumber);
              decodeOrStoreSavedBitmap(null, historyItem.getResult());
            }
          }
        }
      }

      private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
        // Bitmap isn't used yet -- will be used soon
        if (handler == null) {
          savedResultToShow = result;
        } else {
          if (result != null) {
            savedResultToShow = result;
          }
          if (savedResultToShow != null) {
            Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
            handler.sendMessage(message);
          }
          savedResultToShow = null;
        }
      }

      @Override
      public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
          Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
          hasSurface = true;
          initCamera(holder);
        }
      }

      @Override
      public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
      }

      @Override
      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

      }


    //处理扫码结果
      public void handleDecode(final Result rawResult, Bitmap barcode, float scaleFactor) {

          inactivityTimer.onActivity();
           ResultHandler resultHandler = ResultHandlerFactory.makeResultHandler(
           this, rawResult);

          boolean fromLiveScan = barcode != null;

          if (fromLiveScan) {
              historyManager.addHistoryItem(rawResult, resultHandler);
              // Then not from history, so beep/vibrate and we have an image to draw on
              beepManager.playBeepSoundAndVibrate();
              drawResultPoints(barcode, scaleFactor, rawResult);
          }
          AlertDialog.Builder dialog = new AlertDialog.Builder(this);
          if (barcode == null) {
              dialog.setIcon(null);
          } else {

              Drawable drawable = new BitmapDrawable(barcode);
              dialog.setIcon(drawable);
          }
          dialog.setTitle("扫描结果");
          dialog.setMessage(rawResult.getText());
          dialog.setNegativeButton("确定", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {

                  //传递仓品出入库参数
                  Intent intent2 = new Intent(CaptureActivity.this,CaptureActivity.class);
                  if (getTrans)//若为入库
                  {
                      if (getStatus)//若刚扫完仓品条码
                      {
                          intent2.putExtra("isCargo",false);
                          intent2.putExtra("isStore",true);
                      }
                      else //若已扫完仓位码
                      {
                          intent2.putExtra("isCargo",true);
                          intent2.putExtra("isStore",true);
                          //向主站传递入库数据
                      }
                  }
                  else //若为出库
                  {
                      if (getStatus)//若刚扫完仓品条码
                      {
                          intent2.putExtra("isCargo",false);
                      }
                      else //若已扫完仓位码
                      {
                          intent2.putExtra("isCargo",true);
                          //向主站传递出库数据
                      }
                  }
                  startActivity(intent2);




                 /*
                  // 用默认浏览器打开扫描得到的地址
                  final Result result = rawResult;
                  Intent intent = new Intent();
                  intent.setAction("android.intent.action.VIEW");
                  Uri content_url = Uri.parse(result.getText());
                  intent.setData(content_url);
                  startActivity(intent);
                  finish();*/
              }
          });
          dialog.setPositiveButton("取消", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {

                  Intent intent = new Intent(CaptureActivity.this,CaptureActivity.class);
                  intent.putExtra("isCargo",true);
                  if (getTrans)
                  {
                      intent.putExtra("isStore",true);
                  }else
                  {
                      intent.putExtra("isStore",false);
                  }
                  startActivity(intent);
                 // finish();
              }
          });
          /*dialog.setNeutralButton("修改",new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                  //同步数据
                  finish();
              }
          });*/
          dialog.create().show();

          if (fromLiveScan) {
                beepManager.playBeepSoundAndVibrate();

          }

          switch (source) {
              case NATIVE_APP_INTENT:
              case PRODUCT_SEARCH_LINK:
                  // handleDecodeExternally(rawResult, resultHandler, barcode);
                  break;

              case NONE:

                  break;
          }

      }


      private void drawResultPoints(Bitmap barcode, float scaleFactor, Result rawResult) {
        ResultPoint[] points = rawResult.getResultPoints();
        if (points != null && points.length > 0) {
          Canvas canvas = new Canvas(barcode);
          Paint paint = new Paint();
          paint.setColor(getResources().getColor(R.color.result_points));
          if (points.length == 2) {
            paint.setStrokeWidth(4.0f);
            drawLine(canvas, paint, points[0], points[1], scaleFactor);
          } else if (points.length == 4 &&
                     (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A ||
                      rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
            // Hacky special case -- draw two lines, for the barcode and metadata
            drawLine(canvas, paint, points[0], points[1], scaleFactor);
            drawLine(canvas, paint, points[2], points[3], scaleFactor);
          } else {
            paint.setStrokeWidth(10.0f);
            for (ResultPoint point : points) {
              if (point != null) {
                canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
              }
            }
          }
        }
      }

      private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor) {
        if (a != null && b != null) {
          canvas.drawLine(scaleFactor * a.getX(),
                          scaleFactor * a.getY(),
                          scaleFactor * b.getX(),
                          scaleFactor * b.getY(),
                          paint);
        }
      }







      private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
          throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
          Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
          return;
        }
        try {
          cameraManager.openDriver(surfaceHolder);
          // Creating the handler starts the preview, which can also throw a RuntimeException.
          if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
          }
          decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
          Log.w(TAG, ioe);
          displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
          // Barcode Scanner has seen crashes in the wild of this variety:
          // java.?lang.?RuntimeException: Fail to connect to camera service
          Log.w(TAG, "Unexpected error initializing camera", e);
          displayFrameworkBugMessageAndExit();
        }
      }

    //bug
      private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
      }

      public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
          handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        resetStatusView();
      }

    //重置扫码
      private void resetStatusView() {
        //resultView.setVisibility(View.GONE);
        statusView.setText(R.string.msg_default_status);
        statusView.setVisibility(View.VISIBLE);
        viewfinderView.setVisibility(View.VISIBLE);
        lastResult = null;
      }

      public void drawViewfinder() {
        viewfinderView.drawViewfinder();
      }

    @Override
    public void onBackPressed() {
        Intent intent  = new Intent(CaptureActivity.this, IndexActivity.class);
        CaptureActivity.this.finish();
        cameraManager.stopPreview();
        startActivity(intent);
    }
}
