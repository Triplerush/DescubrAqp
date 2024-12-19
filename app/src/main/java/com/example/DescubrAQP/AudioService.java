package com.example.DescubrAQP;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class AudioService extends Service {

    private static final String TAG = "AudioService";
    private static final String CHANNEL_ID = "AudioPlaybackChannel";
    private static final int NOTIFICATION_ID = 1;

    public static final String ACTION_PLAY = "com.example.lab4_fragments.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.lab4_fragments.ACTION_PAUSE";
    public static final String EXTRA_AUDIO_RES_ID = "com.example.lab4_fragments.EXTRA_AUDIO_RES_ID";

    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private int currentAudioResId = 0;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servicio creado");
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_PLAY.equals(action)) {
                int audioResId = intent.getIntExtra(EXTRA_AUDIO_RES_ID, 0);
                if (audioResId != 0) {
                    playAudio(audioResId);
                }
            } else if (ACTION_PAUSE.equals(action)) {
                pauseAudio();
            }
        }

        return START_NOT_STICKY;
    }

    /**
     * Reproduce el audio especificado.
     *
     * @param audioResId ID del recurso de audio
     */
    private void playAudio(int audioResId) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, audioResId);
            currentAudioResId = audioResId;
            mediaPlayer.setOnCompletionListener(mp -> {
                stopForeground(true);
                stopSelf();
                isPlaying = false;
            });
        } else if (currentAudioResId != audioResId) {
            mediaPlayer.reset();
            mediaPlayer = MediaPlayer.create(this, audioResId);
            currentAudioResId = audioResId;
        }

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
            startForeground(NOTIFICATION_ID, buildNotification());
            Log.d(TAG, "Reproducción de audio iniciada");
        }
    }

    /**
     * Pausa la reproducción de audio.
     */
    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            stopForeground(true); // Detiene la notificación
            Log.d(TAG, "Reproducción de audio pausada");
        }
    }

    /**
     * Salta a una posición específica en la reproducción de audio.
     *
     * @param position Posición en milisegundos
     */
    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
            Log.d(TAG, "Saltando a la posición: " + position);
        }
    }

    /**
     * Obtiene la posición actual de la reproducción de audio.
     *
     * @return Posición en milisegundos
     */
    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * Obtiene la duración total del audio.
     *
     * @return Duración en milisegundos
     */
    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    /**
     * Construye la notificación para la reproducción en segundo plano.
     *
     * @return Objeto Notification
     */
    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Reproduciendo Audio")
                .setContentText("La reproducción está en segundo plano")
                .setSmallIcon(R.drawable.ic_notification) // Asegúrate de que este ícono exista
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true); // Evita que el usuario pueda deslizar la notificación

        return builder.build();
    }

    /**
     * Crea el canal de notificación para Android Oreo y superiores.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Reproducción de Audio";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Notificación para la reproducción de audio en segundo plano");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d(TAG, "MediaPlayer liberado y servicio destruido");
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // Se utiliza binding en este servicio
        return binder;
    }
}
