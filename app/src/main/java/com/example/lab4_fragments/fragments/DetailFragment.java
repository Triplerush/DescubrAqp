package com.example.lab4_fragments.fragments;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.lab4_fragments.Building;
import com.example.lab4_fragments.BuildingRepository;
import com.example.lab4_fragments.dao.comment.Comment;
import com.example.lab4_fragments.CommentAdapter;
import com.example.lab4_fragments.R;
import java.util.ArrayList;
import java.util.List;
import com.example.lab4_fragments.database.AppDatabase;

public class DetailFragment extends Fragment {
    private static final String ARG_BUILDING_ID = "building_id";
    private static final String TAG = "DetailFragment";

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
    private MediaPlayer mediaPlayer;
    private Button btnPlayPause;
    private SeekBar audioSeekBar;
    private Handler handler;
    private Runnable updateSeekBarRunnable;

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
            imageView.setImageResource(building.getImageResId());

            // Configurar el MediaPlayer
            String audioFileName = "audedif" + (buildingId + 1); // Sin extensión
            int resId = getResources().getIdentifier(audioFileName, "raw", getContext().getPackageName());

            if (resId != 0) { // Verificar que el recurso exista
                mediaPlayer = MediaPlayer.create(getContext(), resId);
                if (mediaPlayer != null) {
                    setupAudioPlayer();
                } else {
                    Toast.makeText(getContext(), "No se pudo inicializar el reproductor", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "MediaPlayer.create retornó null para el recurso: " + audioFileName);
                }
            } else {
                Toast.makeText(getContext(), "Archivo de audio no encontrado: " + audioFileName, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Recurso de audio no encontrado: " + audioFileName);
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

        return view;
    }

    /**
     * Configura el reproductor de audio, incluyendo los controles de reproducción y la barra de progreso.
     */
    private void setupAudioPlayer() {
        handler = new Handler();

        // Configurar botón de Play/Pause
        btnPlayPause.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnPlayPause.setText("Reproducir");
            } else {
                mediaPlayer.start();
                btnPlayPause.setText("Pausar");
                updateSeekBar();
            }
        });

        // Configurar SeekBar
        audioSeekBar.setMax(mediaPlayer.getDuration());
        audioSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            boolean userTouch = false;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    audioSeekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                userTouch = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                userTouch = false;
            }
        });

        // Listener para cuando la reproducción finaliza
        mediaPlayer.setOnCompletionListener(mp -> {
            btnPlayPause.setText("Reproducir");
            audioSeekBar.setProgress(0);
            handler.removeCallbacks(updateSeekBarRunnable);
        });

        Log.d(TAG, "MediaPlayer configurado correctamente");
    }

    /**
     * Actualiza la barra de progreso del audio cada segundo.
     */
    private void updateSeekBar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            audioSeekBar.setProgress(mediaPlayer.getCurrentPosition());
            updateSeekBarRunnable = this::updateSeekBar;
            handler.postDelayed(updateSeekBarRunnable, 1000);
        }
    }

    /**
     * Carga los comentarios almacenados para el edificio actual desde un archivo.
     */
    private void loadComments() {
        new Thread(() -> {
            commentList.clear();
            commentList.addAll(appDatabase.commentDao().getCommentsForBuilding(buildingId));
            requireActivity().runOnUiThread(() -> commentAdapter.notifyDataSetChanged());
        }).start();
    }

    /**
     * Agrega un nuevo comentario a la lista y lo guarda en el archivo correspondiente.
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
        // Liberar el MediaPlayer para evitar fugas de memoria
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
            Log.d(TAG, "MediaPlayer liberado");
        }

        // Remover callbacks pendientes
        if (handler != null && updateSeekBarRunnable != null) {
            handler.removeCallbacks(updateSeekBarRunnable);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Pausar el MediaPlayer si está reproduciendo
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlayPause.setText("Reproducir");
            Log.d(TAG, "MediaPlayer pausado en onPause");
        }
    }

}
