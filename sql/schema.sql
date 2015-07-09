
CREATE TABLE movie
(
  movie_id serial NOT NULL,
  movie_rt_id integer,
  movie_title character varying(1024),
  movie_synopsis text,
  movie_poster_main character varying(255),
  movie_poster_thumbnail character varying(255),
  movie_genre character varying(1024),
  movie_director character varying(255),
  movie_writer character varying(1024),
  movie_release_date_theater_text character varying(50),
  movie_release_date_dvd_text character varying(50),
  movie_release_date_theater date,
  movie_release_date_dvd date,
  movie_runtime character varying(20),
  movie_box_office_us character varying(20),
  movie_actors text,
  movie_tomato_rating integer,
  movie_url_slug character varying(1024),
  movie_microcritix_rating numeric(3,1),
  movie_hash_tag character varying(1024),
  CONSTRAINT movie_pkey PRIMARY KEY (movie_id)
);



CREATE TABLE movie_tweet(
       movie_tweet_id serial PRIMARY KEY,
       movie_id integer,
       movie_tweet_rating DECIMAL(3,1),
       movie_tweet_user_id bigint,
       movie_tweet_twitter_id bigint,
       movie_tweet_text varchar(255),
       CONSTRAINT movie_tweet_movie_id_id_fkey FOREIGN KEY (movie_id)
       REFERENCES movie (movie_id) MATCH SIMPLE 
       ON DELETE CASCADE
       );
