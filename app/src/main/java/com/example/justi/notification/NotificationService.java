package com.example.justi.notification;
import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import jp.yokomark.remoteview.reader.RemoteViewsInfo;
import jp.yokomark.remoteview.reader.RemoteViewsReader;
import jp.yokomark.remoteview.reader.action.BitmapReflectionAction;
import jp.yokomark.remoteview.reader.action.RemoteViewsAction;


public class NotificationService extends NotificationListenerService {

    /*
        These are the package names of the apps. for which we want to
        listen the notifications
     */
    private static final class ApplicationPackageNames {
        public static final String FACEBOOK_PACK_NAME = "com.facebook.katana";
        public static final String FACEBOOK_MESSENGER_PACK_NAME = "com.facebook.orca";
        public static final String WHATSAPP_PACK_NAME = "com.whatsapp";
        public static final String INSTAGRAM_PACK_NAME = "com.instagram.android";
        public static final String GOOGLE_MAPS_NAME = "com.google.android.apps.maps";
    }

    /*
        These are the return codes we use in the method which intercepts
        the notifications, to decide whether we should do something or not
     */
    public static final class InterceptedNotificationCode {
        public static final int FACEBOOK_CODE = 1;
        public static final int WHATSAPP_CODE = 2;
        public static final int INSTAGRAM_CODE = 3;
        public static final int MAPS_CODE = 4;
        public static final int OTHER_NOTIFICATIONS_CODE = 5; // We ignore all notification with code == 4
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        int notificationCode = matchNotificationCode(sbn);

        //if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE){
        if(notificationCode <= InterceptedNotificationCode.INSTAGRAM_CODE){
            Intent intent = new  Intent("com.example.justi.notification");
            intent.putExtra("Notification Code", notificationCode);
            sendBroadcast(intent);
        }else if(notificationCode == InterceptedNotificationCode.MAPS_CODE){
            Notification notification = sbn.getNotification();
            RemoteViews rv = notification.bigContentView;
            Bundle bundle = notification.extras;


            // We have to extract the information from the view
            RemoteViews        remoteViews = notification.bigContentView;
            if (remoteViews == null) remoteViews = notification.contentView;

            Bitmap bitmap = null;

            RemoteViewsInfo info = RemoteViewsReader.read(this, remoteViews);
            for (RemoteViewsAction action : info.getActions()) {
                if (!(action instanceof BitmapReflectionAction))
                    continue;
                BitmapReflectionAction concrete = (BitmapReflectionAction)action;

                if(concrete.getMethodName().equals("setImageBitmap")){
                    bitmap = concrete.getBitmap();
                }
            }

            // Use reflection to examine the m_actions member of the given RemoteViews object.
            // It's not pretty, but it works.
            //Our own try - Justin
            /*List<String> text = new ArrayList<String>();
            try
            {
                Field field = views.getClass().getDeclaredField("mActions");
                field.setAccessible(true);

                @SuppressWarnings("unchecked")
                ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field.get(views);

                // Find the setText() and setTime() reflection actions
                for (Parcelable p : actions)
                {
                    Parcel parcel = Parcel.obtain();
                    p.writeToParcel(parcel, 0);
                    parcel.setDataPosition(0);

                    // The tag tells which type of action it is (2 is ReflectionAction, from the source)
                    int tag = parcel.readInt();
                    //if (tag != 2) continue;

                    // View ID
                    parcel.readInt();

                    String methodName = parcel.readString();

                    if(methodName.equals("setImageBitmap")){
                        //parcel.readInt();
                        //bitmap = parcel.readParcelable(Bitmap.class.getClassLoader());
                    }

                    if (methodName == null) continue;

                    parcel.recycle();
                }
            }

            // It's not usually good style to do this, but then again, neither is the use of reflection...
            catch (Exception e)
            {
                Log.e("NotificationClassifier", e.toString());
            }*/


            Intent intent = new  Intent("com.example.justi.notification");


            if(bitmap != null){
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                Bundle b = new Bundle();
                b.putByteArray("Bitmap",byteArray);
                intent.putExtra("Bitmap Code", b);
            }


            intent.putExtra("Notification Code", notificationCode);
            intent.putExtra("Icon Code", bundle);


            sendBroadcast(intent);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        int notificationCode = matchNotificationCode(sbn);

        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {

            StatusBarNotification[] activeNotifications = this.getActiveNotifications();

            if(activeNotifications != null && activeNotifications.length > 0) {
                for (int i = 0; i < activeNotifications.length; i++) {
                    if (notificationCode == matchNotificationCode(activeNotifications[i])) {
                        Intent intent = new  Intent("com.example.justi.notification");
                        intent.putExtra("Notification Code", notificationCode);
                        sendBroadcast(intent);
                        break;
                    }
                }
            }
        }
    }

    private int matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        if(packageName.equals(ApplicationPackageNames.FACEBOOK_PACK_NAME)
                || packageName.equals(ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME)){
            return(InterceptedNotificationCode.FACEBOOK_CODE);
        }
        else if(packageName.equals(ApplicationPackageNames.INSTAGRAM_PACK_NAME)){
            return(InterceptedNotificationCode.INSTAGRAM_CODE);
        }
        else if(packageName.equals(ApplicationPackageNames.WHATSAPP_PACK_NAME)){
            return(InterceptedNotificationCode.WHATSAPP_CODE);
        }
        else if(packageName.equals(ApplicationPackageNames.GOOGLE_MAPS_NAME)){
            return(InterceptedNotificationCode.MAPS_CODE);
        }
        else{
            return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }
    }
}