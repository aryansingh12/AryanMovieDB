package com.example.aryansingh.aryanmoviedb.MovieInfo;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aryansingh.aryanmoviedb.CastInfo.CastInfo;
import com.example.aryansingh.aryanmoviedb.MovieConstants;
import com.example.aryansingh.aryanmoviedb.MovieDBClient;
import com.example.aryansingh.aryanmoviedb.Movies.MoviesAdaptor;
import com.example.aryansingh.aryanmoviedb.Movies.MoviesInterface;
import com.example.aryansingh.aryanmoviedb.Movies.Result;
import com.example.aryansingh.aryanmoviedb.R;
//import com.google.android.youtube.player.YouTubeBaseActivity;
import com.example.aryansingh.aryanmoviedb.RoomDatabase.FavoriteDao;
import com.example.aryansingh.aryanmoviedb.RoomDatabase.Movie;
import com.example.aryansingh.aryanmoviedb.RoomDatabase.MovieDatabase;
import com.example.aryansingh.aryanmoviedb.TvShowInfo.EpisodesFragment;
import com.example.aryansingh.aryanmoviedb.TvShowInfo.MoreLikeThis;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MovieInfo extends AppCompatActivity implements YoutubeFragment.OnFragmentInteractionListener{

    Bundle bundle = new Bundle();
    String genre = "";
    Result movieResults;
    String poster_path;
    LinearLayout similarMoviesLayout, revenueLL, budgetLL;
    MoviesAdaptor moviesAdaptor;
    CastAdaptor castAdaptor;
    ArrayList<Result> list;
    ArrayList<Cast> castArrayList;
    ImageView poster,backdrop,like,share,mylist;
    TextView movie_info_overview, movie_info_release_date, movie_info_budget,movie_info_revenue,
            movie_title,movie_rating,movie_duration,genreTextView,showMore;
    ProgressBar progressBar;
    RecyclerView movie_info_similar_recycler_view,movie_info_cast_recycler_view;
    long id;
    ArrayList<Long> likes = new ArrayList<>();
    ArrayList<Long> mylists = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_info);

        final MovieDatabase database = Room.databaseBuilder(getApplicationContext(),MovieDatabase.class,"movie")
                .allowMainThreadQueries()
                .build();
        List<Long> movie_ids = database.favoriteDao().getAllMovieIds();



        similarMoviesLayout = (LinearLayout) findViewById(R.id.similarMovies);

        genreTextView = findViewById(R.id.genreTextView);
        like = findViewById(R.id.like);
        share = findViewById(R.id.share);
        mylist = findViewById(R.id.mylist);
//        poster = findViewById(R.id.poster);
//        backdrop = findViewById(R.id.backdrop);
        movie_info_overview = findViewById(R.id.movie_info_overview);
        movie_rating= findViewById(R.id.movie_rating);
        movie_title= findViewById(R.id.movie_title);
        movie_duration= findViewById(R.id.movie_duration);
        movie_info_release_date = findViewById(R.id.movie_info_release_date);
        progressBar = findViewById(R.id.progressBar);
        movie_info_similar_recycler_view = findViewById(R.id.movie_info_similar_recycler_view);
        movie_info_cast_recycler_view = findViewById(R.id.movie_info_cast_recycler_view);
        showMore = findViewById(R.id.showMore);

            showMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(showMore.getText().equals("Show Less")){
                        showMore.setText("Show More");
                        movie_info_overview.setMaxLines(4);
                    }
                    else{
                        movie_info_overview.setMaxLines(1000);
                        showMore.setText("Show Less");
                    }

                }
            });


        mylist.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if(mylists.contains(id)){
                    mylists.remove(id);
                    mylist.setImageDrawable(getDrawable(R.drawable.ic_add_black_24dp));
                    Toast.makeText(MovieInfo.this,"Removed from your List",Toast.LENGTH_LONG).show();
                }
                else{
                    mylists.add(id);
                    mylist.setImageDrawable(getDrawable(R.drawable.ic_check_black_24dp));
                    Toast.makeText(MovieInfo.this,"Added to your List",Toast.LENGTH_LONG).show();
                }
            }
        });

        like.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if(likes.contains(id)){
                    likes.remove(id);
                    like.setImageDrawable(getDrawable(R.drawable.ic_thumb_up_black_24dp));
                    Toast.makeText(MovieInfo.this,"Removed from Favorites",Toast.LENGTH_LONG).show();
                }
                else{
                    likes.add(id);
                    like.setImageDrawable(getDrawable(R.drawable.ic_thumb_up_blue_24dp));
                    Toast.makeText(MovieInfo.this,"Added to Favorites",Toast.LENGTH_LONG).show();

                    Movie movie = new Movie(id,"","type",poster_path);
                    database.favoriteDao().addMovie(movie);// adding to the db


                }
            }
        });

        list = new ArrayList<>();
        castArrayList = new ArrayList<>();
        moviesAdaptor = new MoviesAdaptor(this, list, new MoviesAdaptor.MoviesClickListener() {
            @Override
            public void onMovieClick(View view, int position) {
                Result movieResults = list.get(position);
                Intent i=new Intent(MovieInfo.this, MovieInfo.class);
                i.putExtra("movieId",  movieResults.getId());
                startActivity(i);
            }
        });
        movie_info_similar_recycler_view.setAdapter(moviesAdaptor);
        movie_info_similar_recycler_view.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));

        castAdaptor = new CastAdaptor(this, castArrayList, new CastAdaptor.MoviesClickListener() {
            @Override
            public void onMovieClick(View view, int position) {
                Cast castResults = castArrayList.get(position);
                Intent i=new Intent(MovieInfo.this, CastInfo.class);
                i.putExtra("castId",  castResults.getId());
                startActivity(i);
            }
        });
        movie_info_cast_recycler_view.setAdapter(castAdaptor);
        movie_info_cast_recycler_view.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));


        Intent i = getIntent();
        id = i.getLongExtra("movieId",0);

        YoutubeFragment fragment = new YoutubeFragment();
        bundle.putLong("id",id);
        fragment.setArguments(bundle);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction()
                .replace(R.id.main, fragment)
                .commit();

//        Intent intent = new Intent(MovieInfo.this,YoutubeFragment.class);
//        intent.putExtra("id",id);

        Retrofit retrofit = MovieDBClient.getClient();
        MoviesInterface moviesInterface = retrofit.create(MoviesInterface.class);

        Call<SimilarMovies> similarMoviesCall = moviesInterface.getSimilarMovies(id,MovieConstants.API_KEY);
        similarMoviesCall.enqueue(new Callback<SimilarMovies>() {
            @Override
            public void onResponse(Call<SimilarMovies> call, Response<SimilarMovies> response) {
                SimilarMovies similarMovies = response.body();
                list.clear();
                list.addAll(similarMovies.getResults());
                if(list.size()!=0){
                    similarMoviesLayout.setVisibility(View.VISIBLE);
                }
                moviesAdaptor.notifyDataSetChanged();
            }
            @Override
            public void onFailure(Call<SimilarMovies> call, Throwable t) {

            }
        });

        final Call<Cast> crewCall = moviesInterface.getCrew(id,MovieConstants.API_KEY);
        crewCall.enqueue(new Callback<Cast>() {
            @Override
            public void onResponse(Call<Cast> call, Response<Cast> response) {
                Cast crewList = response.body();
                castArrayList.clear();
                castArrayList.addAll(crewList.getCast());
                castAdaptor.notifyDataSetChanged();

            }
            @Override
            public void onFailure(Call<Cast> call, Throwable t) {

            }
        });

        Call<MovieDetails> detailsCall = moviesInterface.getMovieDetails(id,MovieConstants.API_KEY);
        detailsCall.enqueue(new Callback<MovieDetails>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(Call<MovieDetails> call, Response<MovieDetails> response) {
                MovieDetails movieDetails = response.body();
//                if (movieDetails.getPosterPath() != null) {
//                    poster.setVisibility(View.VISIBLE);
//                    progressBar.setVisibility(View.GONE);
//                    Picasso.with(MovieInfo.this).load(MovieConstants.MOVIE_IMAGE_BASE_URL + movieDetails.getPosterPath()).into(poster);
//                }
//                if (movieDetails.getBackdropPath() != null) {
//                    backdrop.setVisibility(View.VISIBLE);
//                    progressBar.setVisibility(View.GONE);
//                    Picasso.with(MovieInfo.this).load(MovieConstants.SLIDER_BASE_URL + movieDetails.getBackdropPath()).into(backdrop);
//                }
                poster_path = movieDetails.getPosterPath();
                if (movieDetails.getOverview() != null) {
                    movie_info_overview.setText(movieDetails.getOverview() + "");
                }
                if (movieDetails.getReleaseDate() != null || movieDetails.getReleaseDate().length()!=0) {
                    String year = movieDetails.getReleaseDate().substring(0,4);
                    movie_info_release_date.setText(year);
                }
                if(movieDetails.getRuntime() !=null){
                    movie_duration.setText(changeTime(movieDetails.getRuntime()));
                }
                if(movieDetails.getTitle()!=null){
                    movie_title.setText(movieDetails.getTitle());
                }
                if(movieDetails.getVoteAverage()!=null){
                    movie_rating.setText("Imdb: " + movieDetails.getVoteAverage() + "");
                }
                if(movieDetails.getGenres().size()!=0) {
                    for (int i = 0; i < movieDetails.getGenres().size(); i++) {
                        genre = genre.concat(movieDetails.getGenres().get(i).getName() + " . ");
                    }
                    genre = genre.substring(0,genre.length()-2);
                    genreTextView.setText(genre + "");
                }
            }

            @Override
            public void onFailure(Call<MovieDetails> call, Throwable t) {

            }
        });

    }

    private String changeTime(Long runtime) {

        long hrs = runtime/60;
        long mins = runtime%60;
        return "" + hrs + " hrs " + mins + " mins";

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private class PagerAdaptor extends FragmentStatePagerAdapter {

        int nTabs;

        public PagerAdaptor(FragmentManager fm, int nTabs) {
            super(fm);
            this.nTabs = nTabs;
        }

        @Override
        public Fragment getItem(int position) {

//            switch (position){
//                case 0:
//                    YoutubeFragment youtubeFragment = new YoutubeFragment();
//
//                    return youtubeFragment;
//                default:
                    return null;
            //}
        }

        @Override
        public int getCount() {
            return nTabs;
        }
    }

}
