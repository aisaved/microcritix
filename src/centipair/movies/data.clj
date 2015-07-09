(ns centipair.movies.data
    (:require [clj-http.client :as client]
              [taoensso.timbre :as timbre]
              [cheshire.core :refer [parse-string]]
              [centipair.movies.models :refer [create-movie
                                               update-release-dates
                                               microcritix-rating
                                               title-hash]]
              [slugger.core :as slugger]
              [clojure.core.async
               :as a
               :refer [>! <! >!! <!! go chan buffer close! thread
                       alts! alts!! timeout]]))



;; rotten tomatoes url sample : "http://www.rottentomatoes.com/api/private/v1.0/m/list/find?page=1&limit=30&type=dvd-all&services=amazon%3Bamazon_prime%3Bflixster%3Bhbo_go%3Bitunes%3Bnetflix_iw%3Bvudu&sortBy=release"

;;omdb samplel url : "http://www.omdbapi.com/?t=KINGSMAN%3A+THE+SECRET+SERVICE&y=&plot=short&r=json&tomatoes=true"

(def movie-channel (chan))
(def dvd-channel (chan))

(def rt-details-channel (chan))

(def omdb-base-url "http://www.omdbapi.com/")
(def rt-direct-base-url "http://www.rottentomatoes.com/api/private/v1.0/m/list/find")

(def rt-api-key "54whembgwjzstgs3gdujxxuw")

(def rt-base-url "http://api.rottentomatoes.com/api/public/v1.0/movies/")


(defn fetch-data
  [url query-params]
  (let [response-rt (client/get url {:accept :json
                                     :query-params query-params})
        response-body (:body response-rt)
        response-json (parse-string response-body true)]
    response-json))


(defn fetch-rt-detailed
  [rt-id]
  (fetch-data (str rt-base-url rt-id ".json")
              {:apikey rt-api-key}))


(defn fetch-omdb
  [title]
  (fetch-data omdb-base-url
              {:t title
               :r "json"
               :plot "full"
               :tomatoes true
               }))


(defn fetch-rt-dvd
  [page-limit]
  (fetch-data rt-direct-base-url
              {:page (:page page-limit)
               :limit (:limit page-limit)
               :type "dvd-all"
               :services "amazon;amazon_prime;flixster;hbo_go;itunes;netflix_iw;vudu"
               :sortBy "release"}))



(defn add-release-dates
  [rt-id]
  (let [rt-details-data (fetch-rt-detailed rt-id)]
    (update-release-dates rt-id (:release_dates rt-details-data))))


(defn omdb-params
  [omdb-movie rt-movie]
  {:movie_rt_id (:id rt-movie)
   :movie_title (:title rt-movie)
   :movie_synopsis (:Plot omdb-movie)
   :movie_poster_main (:secondary (:posters rt-movie))
   :movie_poster_thumbnail (:primary (:posters rt-movie))
   :movie_genre (:Genre omdb-movie)
   :movie_director (:Director omdb-movie)
   :movie_writer (:Writer omdb-movie)
   :movie_release_date_theater_text (:Released omdb-movie)
   :movie_release_date_dvd_text (:DVD omdb-movie)
   :movie_runtime (:Runtime omdb-movie)
   :movie_box_office_us (:BoxOffice omdb-movie)
   :movie_actors (:Actors omdb-movie)
   :movie_tomato_rating (:tomatoScore rt-movie)
   :movie_url_slug (slugger/->slug (:title rt-movie))
   :movie_microcritix_rating (microcritix-rating (:tomatoScore rt-movie))
   :movie_hash_tag (title-hash (:title rt-movie))})



(defn save-movie
  "Params required
  movie_rt_id integer,
  movie_title varchar(255),
  movie_synopsis text,
  movie_poster_main varchar(255),
  movie_poster_thumbnail varchar(255),
  movie_genre varchar(50),
  movie_director varchar (255),
  movie_writer varchar(255),
  movie_release_date_theater_text date,
  movie_release_date_dvd_text date,
  movie_release_date_theater date,
  movie_release_date_dvd date,
  movie_runtime varchar(10),
  movie_box_office_us varchar(10),
  movie_actors text,
  movie_tomato_rating integer,
  "
  [rt-movie]
  (let [omdb-movie (fetch-omdb (:title rt-movie))]
    (timbre/info "Saving movie to db")
    (if (nil? (:Title omdb-movie))
      (timbre/warn "Movie not found")
      (let [movie-db (create-movie (omdb-params omdb-movie rt-movie))]
        (go 
          (>! rt-details-channel (:id rt-movie)))))))


(defn process-dvd
  [page-limit]
  (let [dvd-data (:results (fetch-rt-dvd page-limit))]
    (doseq [each dvd-data]
      (go
        (>! movie-channel each)))))


(defn init-rt-details-channel
  []
  (go 
    (while true
      (add-release-dates (<! rt-details-channel)))))


(defn init-movie-channel
  []
  (go 
    (while true
      (save-movie (<! movie-channel)))))


(defn init-dvd-channel
  []
  (go 
    (while true
      (process-dvd (<! dvd-channel)))))


(defn initialize-movie-channels
  []
  (do
    (init-dvd-channel)
    (init-movie-channel)
    (init-rt-details-channel)))


(defn init-data
  [start end limit]
  (doseq [each (range start end)]
    (go
      (>! dvd-channel {:page each :limit limit}))))
