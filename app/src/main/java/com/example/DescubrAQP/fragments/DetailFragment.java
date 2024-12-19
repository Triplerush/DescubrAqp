package com.example.DescubrAQP.fragments;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.DescubrAQP.dao.building.Building;
import com.example.DescubrAQP.BuildingRepository;
import com.example.DescubrAQP.dao.comment.Comment;
import com.example.DescubrAQP.CommentAdapter;
import com.example.DescubrAQP.R;
import com.example.DescubrAQP.AudioService;
import com.example.DescubrAQP.database.AppDatabase;
import java.util.ArrayList;
import java.util.List;

public class DetailFragment extends Fragment {
    private static final String ARG_BUILDING_ID = "building_id";
    private static final String TAG = "DetailFragment";
    private static final int REQUEST_CODE_POST_NOTIFICATIONS = 1001;

    private int buildingId;
    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;
    private Button btnView360;
    private Button btnViewMansion;
    private EditText commentInput;
    private Button submitCommentButton;
    private RatingBar ratingBar;
    private BuildingRepository buildingRepository;
    private Building building;
    private Button btnPlayPause;
    private SeekBar audioSeekBar;
    private boolean isPlaying = false;
    private int audioResId = 0;

    // Variables para el Servicio Vinculado
    private AudioService audioService;
    private boolean isBound = false;
    private Handler handler = new Handler();
    private Runnable updateSeekBarRunnable;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.LocalBinder binder = (AudioService.LocalBinder) service;
            audioService = binder.getService();
            isBound = true;
            initializeSeekBar();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            audioService = null;
        }
    };

    private AppDatabase appDatabase;

    public static DetailFragment newInstance(int buildingId) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_BUILDING_ID, buildingId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            buildingId = getArguments().getInt(ARG_BUILDING_ID);
        }

        // Inflar el diseño del fragmento
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        appDatabase = AppDatabase.getInstance(getContext());

        // Inicializar las vistas
        ImageView imageView = view.findViewById(R.id.image_view);
        TextView titleTextView = view.findViewById(R.id.title_text_view);
        TextView descriptionTextView = view.findViewById(R.id.description_text_view);

        btnView360 = view.findViewById(R.id.btn_view_360);




        btnViewMansion = view.findViewById(R.id.btn_view_mansion);
        commentInput = view.findViewById(R.id.comment_input);
        submitCommentButton = view.findViewById(R.id.submit_comment_button);
        ratingBar = view.findViewById(R.id.rating_bar);
        btnPlayPause = view.findViewById(R.id.btn_play_pause);
        audioSeekBar = view.findViewById(R.id.audio_seek_bar);

        // Configurar RecyclerView para comentarios
        commentsRecyclerView = view.findViewById(R.id.comments_recycler_view);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList);
        commentsRecyclerView.setAdapter(commentAdapter);

        // Inicializar el repositorio de edificios y obtener la lista
        buildingRepository = new BuildingRepository(getContext());
        List<Building> buildingList = buildingRepository.getBuildingList();

        // Verificar si el ID del edificio es válido
        if (buildingId >= 0 && buildingId < buildingList.size()) {
            building = buildingList.get(buildingId);
            titleTextView.setText(building.getTitle());
            descriptionTextView.setText(building.getDescription());
            imageView.setImageResource(Integer.parseInt(building.getImageResId()));

            // Para el botón Vista360
            if (building.getTitle().equals("Catedral")) {
                btnView360.setEnabled(true);
                btnView360.setOnClickListener(v -> {
                    Vista360Fragment vista360Fragment = new Vista360Fragment();
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainerView, vista360Fragment)
                            .addToBackStack(null)
                            .commit();
                });
            } else {
                btnView360.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "En construcción", Toast.LENGTH_SHORT).show();
                });
                btnView360.setEnabled(false);
            }

// Para el botón Mansión
            if (building.getTitle().equals("Mansión del Fundador")) {
                btnViewMansion.setEnabled(true);
                btnViewMansion.setOnClickListener(v -> {
                    MansionFragment mansionFragment = new MansionFragment();
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainerView, mansionFragment)
                            .addToBackStack(null)
                            .commit();
                });
            } else {
                btnViewMansion.setOnClickListener(v -> {
                    Toast.makeText(getContext(), "En construcción", Toast.LENGTH_SHORT).show();
                });
                btnViewMansion.setEnabled(false);
            }

            // Configurar el audio
            String audioFileName = "audedif" + (buildingId + 1); // Sin extensión
            audioResId = getResources().getIdentifier(audioFileName, "raw", getContext().getPackageName());

            if (audioResId != 0) { // Verificar que el recurso exista
                btnPlayPause.setEnabled(true);
                audioSeekBar.setEnabled(false); // Inicialmente deshabilitada hasta que se vincule el servicio
            } else {
                Toast.makeText(getContext(), "Archivo de audio no encontrado: " + audioFileName, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Recurso de audio no encontrado: " + audioFileName);
                btnPlayPause.setEnabled(false);
                audioSeekBar.setEnabled(false);
            }
        } else {
            // Manejar el caso donde el edificio no se encuentra
            titleTextView.setText("Edificación no encontrada");
            descriptionTextView.setText("");
            imageView.setImageResource(R.drawable.mapimage);
            Toast.makeText(getContext(), "Edificación no encontrada", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ID de edificio inválido: " + buildingId);
        }

        // Cargar comentarios existentes
        loadComments();

        // Verificar y solicitar permiso de notificaciones si es necesario
        checkAndRequestNotificationPermission();

        // Configurar listeners para los botones
        btnView360.setOnClickListener(v -> {
            Vista360Fragment vista360Fragment = new Vista360Fragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, vista360Fragment)
                    .addToBackStack(null)
                    .commit();
        });

        btnViewMansion.setOnClickListener(v -> {
            MansionFragment mansionFragment = new MansionFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, mansionFragment)
                    .addToBackStack(null)
                    .commit();
        });

        submitCommentButton.setOnClickListener(v -> addComment());

        // Configurar botón de Play/Pause
        btnPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                pauseAudio();
            } else {
                playAudio();
            }
        });

        // Configurar el listener de la SeekBar
        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean userIsSeeking = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound && audioService != null) {
                    // Opcional: Puedes mostrar la posición actual mientras el usuario arrastra
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userIsSeeking = true;
                // Opcional: Pausar la actualización automática mientras el usuario interactúa
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isBound && audioService != null) {
                    int newPosition = seekBar.getProgress();
                    audioService.seekTo(newPosition);
                    Log.d(TAG, "SeekBar movida a: " + newPosition);
                }
                userIsSeeking = false;
            }
        });

        return view;
    }


    /**
     * Vincula el fragmento al AudioService cuando el fragmento se inicia.
     */
    @Override
    public void onStart() {
        super.onStart();
        if (audioResId != 0) {
            Intent intent = new Intent(getContext(), AudioService.class);
            getContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * Desvincula el fragmento del AudioService cuando el fragmento se detiene.
     */
    @Override
    public void onStop() {
        super.onStop();
        if (isBound) {
            getContext().unbindService(serviceConnection);
            isBound = false;
        }
        handler.removeCallbacks(updateSeekBarRunnable);
    }

    /**
     * Inicializa la SeekBar una vez que el servicio está vinculado.
     */
    private void initializeSeekBar() {
        if (audioService != null) {
            int duration = audioService.getDuration();
            audioSeekBar.setMax(duration);
            audioSeekBar.setEnabled(true);

            // Actualizar la SeekBar periódicamente
            updateSeekBarRunnable = new Runnable() {
                @Override
                public void run() {
                    if (audioService != null && isPlaying) {
                        int currentPosition = audioService.getCurrentPosition();
                        audioSeekBar.setProgress(currentPosition);
                        handler.postDelayed(this, 1000); // Actualizar cada segundo
                    }
                }
            };
            handler.post(updateSeekBarRunnable);
        }
    }

    /**
     * Verifica y solicita el permiso de notificación en tiempo de ejecución si es necesario.
     */
    private void checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Solicitar el permiso
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_CODE_POST_NOTIFICATIONS);
            }
        }
    }

    /**
     * Maneja la respuesta de la solicitud de permisos.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso otorgado, puedes continuar
                Log.d(TAG, "Permiso de notificación otorgado");
            } else {
                // Permiso denegado, notifica al usuario que las notificaciones están deshabilitadas
                Toast.makeText(getContext(),
                        "Permiso de notificación denegado. Algunas funcionalidades pueden no funcionar correctamente.",
                        Toast.LENGTH_LONG).show();
                Log.w(TAG, "Permiso de notificación denegado");
            }
        }
    }

    /**
     * Inicia la reproducción de audio enviando una intención al AudioService.
     */
    private void playAudio() {
        if (audioResId == 0) {
            Toast.makeText(getContext(), "No hay audio para reproducir", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent playIntent = new Intent(getContext(), AudioService.class);
        playIntent.setAction(AudioService.ACTION_PLAY);
        playIntent.putExtra(AudioService.EXTRA_AUDIO_RES_ID, audioResId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireContext().startForegroundService(playIntent);
        } else {
            requireContext().startService(playIntent);
        }

        btnPlayPause.setText("Pausar");
        isPlaying = true;
        Log.d(TAG, "Enviado intent para reproducir audio");

        // Iniciar la actualización de la SeekBar
        if (isBound) {
            initializeSeekBar();
        }
    }

    /**
     * Pausa la reproducción de audio enviando una intención al AudioService.
     */
    private void pauseAudio() {
        Intent pauseIntent = new Intent(getContext(), AudioService.class);
        pauseIntent.setAction(AudioService.ACTION_PAUSE);
        requireContext().startService(pauseIntent);

        btnPlayPause.setText("Reproducir");
        isPlaying = false;
        Log.d(TAG, "Enviado intent para pausar audio");
    }

    /**
     * Carga los comentarios almacenados para el edificio actual desde la base de datos.
     */
    private void loadComments() {
        new Thread(() -> {
            commentList.clear();
            commentList.addAll(appDatabase.commentDao().getCommentsForBuilding(buildingId));
            requireActivity().runOnUiThread(() -> commentAdapter.notifyDataSetChanged());
        }).start();
    }

    /**
     * Agrega un nuevo comentario a la lista y lo guarda en la base de datos.
     */
    private void addComment() {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", getActivity().MODE_PRIVATE);
        String loggedInUser = sharedPreferences.getString("loggedInUser", "Usuario");
        String commentText = commentInput.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (!commentText.isEmpty()) {
            Comment newComment = new Comment(buildingId, loggedInUser, commentText, rating);

            new Thread(() -> {
                appDatabase.commentDao().insertComment(newComment);

                requireActivity().runOnUiThread(() -> {
                    commentList.add(newComment);
                    commentAdapter.notifyItemInserted(commentList.size() - 1);
                    commentsRecyclerView.scrollToPosition(commentList.size() - 1);
                    commentInput.setText("");
                    ratingBar.setRating(0);
                    Toast.makeText(getContext(), "Comentario agregado", Toast.LENGTH_SHORT).show();
                });
            }).start();
        } else {
            Toast.makeText(getContext(), "Por favor, ingresa un comentario.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isPlaying) {
            pauseAudio();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
