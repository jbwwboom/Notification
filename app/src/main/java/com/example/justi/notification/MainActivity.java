package com.example.justi.notification;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";

    private NotificationBroadcastReceiver broadcastReceiver;
    private AlertDialog enableNotificationListenerAlertDialog;
    private TextView textView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If the user did not turn the notification listener service on we prompt him to do so
        if(!isNotificationServiceEnabled()){
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
            enableNotificationListenerAlertDialog.show();
        }

        textView = findViewById(R.id.textView);
        imageView = findViewById(R.id.imageView);

        broadcastReceiver = new NotificationBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.justi.notification");
        registerReceiver(broadcastReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     * @return True if eanbled, false otherwise.
     */
    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Change Intercepted Notification Image
     * Changes the MainActivity image based on which notification was intercepted
     * @param notificationCode The intercepted notification code
     */
    private void changeInterceptedNotificationImage(int notificationCode){
        switch(notificationCode){
            case NotificationService.InterceptedNotificationCode.FACEBOOK_CODE:
                textView.setText("Facebook");
                break;
            case NotificationService.InterceptedNotificationCode.INSTAGRAM_CODE:
                textView.setText("Instagram");
                break;
            case NotificationService.InterceptedNotificationCode.WHATSAPP_CODE:
                textView.setText("Whatsapp");
                break;
            case NotificationService.InterceptedNotificationCode.MAPS_CODE:
                textView.setText("Maps");
                break;
            case NotificationService.InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE:
                textView.setText("Other");
                break;
        }
    }



    public class NotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int receivedNotificationCode = intent.getIntExtra("Notification Code",-1);
            changeInterceptedNotificationImage(receivedNotificationCode);

            if(receivedNotificationCode == NotificationService.InterceptedNotificationCode.MAPS_CODE){
                Bundle bitmap = intent.getBundleExtra("Bitmap Code");
                if(bitmap != null){
                    byte[] array = bitmap.getByteArray("Bitmap");
                    Bitmap bmp = BitmapFactory.decodeByteArray(array, 0, array.length);
                    imageView.setImageBitmap(bmp);
                }
                /*String pack= "com.google.android.apps.maps"; // ex. for whatsapp;
                Bundle extras = intent.getBundleExtra("Icon Code");

                int iconId = extras.getInt(Notification.EXTRA_SMALL_ICON); // SMALL ICON
                int id = 2130903048; //ICON

                try {
                    PackageManager manager = getPackageManager();
                    Resources resources = manager.getResourcesForApplication(pack);

                    //Drawable icon = resources.getDrawable(iconId);
                    Drawable icon = resources.getDrawable(id);
                    imageView.setImageDrawable(icon);


                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                if (extras.containsKey(Notification.EXTRA_LARGE_ICON_BIG)) {
                    // this bitmap contain the picture attachment
                    Bitmap bmp = (Bitmap) extras.get(Notification.EXTRA_LARGE_ICON_BIG);
                }*/
            }
        }
    }


    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Notification Service");
        alertDialogBuilder.setMessage("New Notification");
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
                    }
                });
        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                    }
                });
        return(alertDialogBuilder.create());
    }
}
