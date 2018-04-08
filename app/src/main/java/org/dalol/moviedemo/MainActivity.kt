package org.dalol.moviedemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_movie_layout.*
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var movieAdapter : MoviesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        movieAdapter = MoviesAdapter()
        movies_list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        movies_list.adapter = movieAdapter

        val retrofit : Retrofit = Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()

        val apiMovies = retrofit.create(ApiMovies::class.java)

        apiMovies.getMovies()
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ movieAdapter.setMovies(it.data) },
                        {
                            Toast.makeText(applicationContext, it.message, Toast.LENGTH_SHORT).show()
                        })
    }

    inner class MoviesAdapter : RecyclerView.Adapter<MoviesAdapter.MovieViewHolder>() {

        private val movies: MutableList<Movie> = mutableListOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
            return MovieViewHolder(layoutInflater.inflate(R.layout.item_movie_layout, parent, false))
        }

        override fun getItemCount(): Int {
            return movies.size
        }

        override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
            holder.bindModel(movies[position])
        }

        fun setMovies(data: List<Movie>) {
            movies.addAll(data)
            notifyDataSetChanged()
        }

        inner class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val movieTitleTxt : TextView = itemView.findViewById(R.id.movieTitle)
            val movieGenreTxt : TextView = itemView.findViewById(R.id.movieGenre)
            val movieYearTxt : TextView = itemView.findViewById(R.id.movieYear)
            val movieAvatarImage : ImageView = itemView.findViewById(R.id.movieAvatar)

            fun bindModel(movie: Movie) {
                movieTitleTxt.text = movie.title
                movieGenreTxt.text = movie.genre
                movieYearTxt.text = movie.year
                Picasso.get().load(movie.poster).into(movieAvatarImage)
            }
        }
    }
}
