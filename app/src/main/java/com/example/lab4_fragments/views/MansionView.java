package com.example.lab4_fragments.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.example.lab4_fragments.dao.mansion.Door;
import com.example.lab4_fragments.R;
import com.example.lab4_fragments.dao.mansion.RoomEntity;
import com.example.lab4_fragments.dao.mansion.RoomInfo;
import com.example.lab4_fragments.database.AppDatabase;
import com.example.lab4_fragments.fragments.DetailRoomFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import android.util.Log;
import android.view.MotionEvent;

import androidx.fragment.app.FragmentActivity;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MansionView extends View {

    private Paint paintRoomOutline;
    private Paint paintDoor;
    private Paint paintText;
    private Paint paintTextVertical;
    private static final String TAG = "MansionView";

    private List<RoomEntity> rooms = new ArrayList<>();
    private List<Door> doors = new ArrayList<>();
    private Map<Integer, RoomInfo> roomDataMap = new HashMap<>();

    private AppDatabase appDatabase;

    public MansionView(Context context) {
        super(context);
        init();
        appDatabase = AppDatabase.getInstance(context);
        insertInitialData(context, this::loadFromDatabase);
    }

    public MansionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        appDatabase = AppDatabase.getInstance(context);
        insertInitialData(context, this::loadFromDatabase);
    }

    public MansionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        appDatabase = AppDatabase.getInstance(context);
        insertInitialData(context, this::loadFromDatabase);
    }

    private void init() {
        // Inicializar el Paint para los contornos de los cuartos
        paintRoomOutline = new Paint();
        paintRoomOutline.setColor(Color.DKGRAY);
        paintRoomOutline.setStyle(Paint.Style.STROKE);
        paintRoomOutline.setStrokeWidth(5);

        // Inicializar el Paint para las puertas
        paintDoor = new Paint();
        paintDoor.setColor(Color.YELLOW);
        paintDoor.setStrokeWidth(10);

        // Inicializar el Paint para el texto horizontal
        paintText = new Paint();
        paintText.setColor(Color.BLACK);
        paintText.setTextSize(30);
        paintText.setAntiAlias(true);

        // Inicializar el Paint para el texto vertical
        paintTextVertical = new Paint();
        paintTextVertical.setColor(Color.BLACK);
        paintTextVertical.setTextSize(30);
        paintTextVertical.setAntiAlias(true);
    }

    private void loadFromDatabase() {
        new Thread(() -> {
            rooms.clear();
            doors.clear();
            roomDataMap.clear();

            // Cargar cuartos
            List<RoomEntity> roomEntities = appDatabase.mansionDao().getAllRooms();
            for (RoomEntity roomEntity : roomEntities) {
                rooms.add(new RoomEntity(roomEntity.getName(), roomEntity.getX1(), roomEntity.getY1(), roomEntity.getX2(), roomEntity.getY2()));
            }

            // Cargar puertas
            List<Door> doorEntities = appDatabase.mansionDao().getAllDoors();
            for (Door doorEntity : doorEntities) {
                doors.add(new Door(doorEntity.getX1(), doorEntity.getY1(), doorEntity.getX2(), doorEntity.getY2()));
            }

            // Cargar información de los cuartos
            List<RoomInfo> roomInfoEntities = appDatabase.mansionDao().getAllRoomInfo();
            for (RoomInfo roomInfoEntity : roomInfoEntities) {
                roomDataMap.put(roomInfoEntity.getId(), new RoomInfo(roomInfoEntity.getTitle(), roomInfoEntity.getDescription(), roomInfoEntity.getImageUrl()));
            }

            // Refrescar la vista
            postInvalidate();
        }).start();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float touchX = event.getX();
            float touchY = event.getY();

            Log.d(TAG, "Touch detected at: (" + touchX + ", " + touchY + ")");

            for (RoomEntity room : rooms) {
                if (isTouchInsideRoom(touchX, touchY, room)) {
                    Log.d(TAG, "Touched inside: " + room.getName());
                    handleRoomTouch(room);
                    return true;
                }
            }
            Log.d(TAG, "No room touched.");
        }
        return super.onTouchEvent(event);
    }

    /**
     * Verifica si el toque está dentro de un cuarto específico.
     *
     * @param touchX Coordenada X del toque
     * @param touchY Coordenada Y del toque
     * @param room   Cuarto a verificar
     * @return Verdadero si el toque está dentro del cuarto, falso en caso contrario
     */
    private boolean isTouchInsideRoom(float touchX, float touchY, RoomEntity room) {
        float x1 = room.getX1();
        float y1 = room.getY1();
        float x2 = room.getX2();
        float y2 = room.getY2();

        boolean inside = (touchX >= x1 && touchX <= x2 && touchY >= y2 && touchY <= y1);
        Log.d(TAG, "Checking room " + room.getName() + ": (" + x1 + ", " + y1 + ") to (" + x2 + ", " + y2 + ") - Inside: " + inside);
        return inside;
    }

    /**
     * Maneja el evento de toque en un cuarto específico.
     *
     * @param room Cuarto que ha sido tocado
     */
    private void handleRoomTouch(RoomEntity room) {
        String name = room.getName();
        String roomNumberStr = name.replaceAll("[^0-9]", "");

        if (!roomNumberStr.isEmpty()) {
            try {
                int roomNumber = Integer.parseInt(roomNumberStr);
                RoomInfo roomInfo = roomDataMap.get(roomNumber);
                if (roomInfo != null) {
                    showRoomDetails(roomInfo);
                } else {
                    Log.d(TAG, "Información no encontrada para Cuarto " + roomNumber);
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Error al convertir el número de cuarto", e);
            }
        } else {
            Log.d(TAG, "Número de cuarto no encontrado en el nombre: " + name);
        }
    }

    /**
     * Muestra los detalles del cuarto seleccionado en un fragmento.
     *
     * @param roomInfo Información del cuarto
     */
    private void showRoomDetails(RoomInfo roomInfo) {
        DetailRoomFragment detailRoomFragment = DetailRoomFragment.newInstance(
                roomInfo.getTitle(), roomInfo.getImageUrl(), roomInfo.getDescription()
        );
        FragmentActivity activity = (FragmentActivity) getContext();
        activity.getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, detailRoomFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Dibujar los contornos de los cuartos
        for (RoomEntity room : rooms) {
            float x1 = room.getX1();
            float y1 = room.getY1();
            float x2 = room.getX2();
            float y2 = room.getY2();

            canvas.drawRect(x1, y1, x2, y2, paintRoomOutline);

            // Dibujar el nombre del cuarto en vertical
            float textX = (x1 + x2) / 2;
            float textY = (y1 + y2) / 2;

            canvas.save(); // Guardar el estado actual del lienzo
            canvas.translate(textX, textY); // Mover el origen al centro del cuarto
            canvas.rotate(-90); // Rotar el lienzo 90 grados en sentido horario
            // Ajustar la posición del texto para que esté centrado
            canvas.drawText(room.getName(), -paintTextVertical.measureText(room.getName()) / 2, 0, paintTextVertical);
            canvas.restore(); // Restaurar el estado original del lienzo

            // Log de las coordenadas dibujadas
            Log.d(TAG, "Dibujando " + room.getName() + " desde (" + x1 + ", " + y1 + ") a (" + x2 + ", " + y2 + ")");
        }

        // Dibujar las puertas como líneas amarillas
        for (Door door : doors) {
            float doorX1 = door.getX1();
            float doorY1 = door.getY1();
            float doorX2 = door.getX2();
            float doorY2 = door.getY2();
            canvas.drawLine(doorX1, doorY1, doorX2, doorY2, paintDoor);

            // Log de las coordenadas de puertas dibujadas
            Log.d(TAG, "Dibujando Puerta desde (" + doorX1 + ", " + doorY1 + ") a (" + doorX2 + ", " + doorY2 + ")");
        }
    }

    private void insertInitialData(Context context, Runnable onInsertComplete) {
        new Thread(() -> {
            if (appDatabase.mansionDao().getAllRooms().isEmpty() &&
                    appDatabase.mansionDao().getAllDoors().isEmpty()) {

                try {
                    InputStream coordinatesStream = context.getAssets().open("coordenadas.txt");
                    BufferedReader coordinatesReader = new BufferedReader(new InputStreamReader(coordinatesStream));
                    String line;

                    while ((line = coordinatesReader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) {
                            continue; // Ignorar líneas vacías y comentarios
                        }

                        String[] parts = line.split(" ");
                        if (parts[0].equalsIgnoreCase("Cuarto")) {
                            // Insertar cuarto en la base de datos
                            String name = parts[1];
                            float x1 = Float.parseFloat(parts[2]);
                            float y1 = Float.parseFloat(parts[3]);
                            float x2 = Float.parseFloat(parts[4]);
                            float y2 = Float.parseFloat(parts[5]);
                            appDatabase.mansionDao().insertRoom(new RoomEntity(name, x1, y1, x2, y2));
                        } else if (parts[0].equalsIgnoreCase("Puerta")) {
                            // Insertar puerta en la base de datos
                            float x1 = Float.parseFloat(parts[1]);
                            float y1 = Float.parseFloat(parts[2]);
                            float x2 = Float.parseFloat(parts[3]);
                            float y2 = Float.parseFloat(parts[4]);
                            appDatabase.mansionDao().insertDoor(new Door(x1, y1, x2, y2));
                        }
                    }
                    coordinatesReader.close();

                    // Leer y procesar datos de cuartos.txt
                    InputStream roomInfoStream = context.getAssets().open("cuartos.txt");
                    BufferedReader roomInfoReader = new BufferedReader(new InputStreamReader(roomInfoStream));
                    String title = null, description = null, imageUrl = null;
                    int roomNumber = -1;

                    while ((line = roomInfoReader.readLine()) != null) {
                        if (line.startsWith("Cuarto:")) {
                            roomNumber = Integer.parseInt(line.split(":")[1].trim());
                        } else if (line.startsWith("Título:")) {
                            title = line.split(":")[1].trim();
                        } else if (line.startsWith("Descripción:")) {
                            description = line.substring(line.indexOf(":") + 1).trim();
                        } else if (line.startsWith("URL de la imagen:")) {
                            imageUrl = line.split(":")[1].trim();

                            // Convertir URL de imagen en recurso drawable
                            int imageResId = context.getResources().getIdentifier(imageUrl, "drawable", context.getPackageName());

                            if (roomNumber != -1 && title != null && description != null && imageResId != 0) {
                                appDatabase.mansionDao().insertRoomInfo(new RoomInfo(title, description, imageResId));
                            }

                            // Reiniciar variables
                            roomNumber = -1;
                            title = null;
                            description = null;
                            imageUrl = null;
                        }
                    }
                    roomInfoReader.close();

                    Log.d("MansionView", "Datos iniciales insertados en la base de datos.");
                } catch (IOException e) {
                    Log.e("MansionView", "Error al leer archivos de datos iniciales", e);
                }
            } else {
                Log.d("MansionView", "Los datos iniciales ya están insertados en la base de datos.");
            }

            // Ejecutar el callback una vez completada la inserción
            if (onInsertComplete != null) {
                post(onInsertComplete);
            }
        }).start();
    }

}