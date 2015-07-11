(ns centipair.movies.models
  (:use korma.db
        centipair.core.db.connection)
  (:require [centipair.core.contrib.time :as time]
            [centipair.core.utilities.pagination :as pagination]
            [clojure.math.numeric-tower :as math]
            [taoensso.timbre :as timbre]
            [korma.core :as korma :refer [insert
                                          delete
                                          select
                                          where
                                          set-fields
                                          values
                                          fields
                                          offset
                                          limit
                                          defentity
                                          order
                                          exec-raw]]))


(defentity movie)
(defentity movie_tweet)

(defn round-places [number decimals]
  (let [factor (math/expt 10 decimals)]
    (bigdec (/ (math/round (* factor number)) factor))))


(defn microcritix-rating
  "conversts tomato ratingto microcritix rating"
  [tomato-rating]
  (if (nil? tomato-rating)
    0
    (round-places (/ tomato-rating 10) 1)))


(defn clean-ambrasand
  "replaces amprasands to 'and'"
  [title]
  (clojure.string/replace title #"&" "and")) 


(defn clean-special-chars
  [title]
  (clojure.string/replace title #"[\\(\\)'-/,:!$%^&*+. ]" ""))


(def title-hash (comp
                 clojure.string/lower-case
                 clean-special-chars
                 clean-ambrasand))


(defn get-movie-rt [rt-id]
  (select movie (fields :movie_rt_id) (where {:movie_rt_id (Integer. rt-id)})))


(defn get-movie [id]
  (first (select movie (where {:movie_id (Integer. id)}))))


(defn get-movie-tweets
  [movie-id]
  (into [] (select movie_tweet (where {:movie_id (Integer. movie-id)}))))


(defn get-movie-url
  [id url-slug]
  (let [movie-obj (get-movie id)
        movie-tweets (get-movie-tweets id)]
    (if (= (:movie_url_slug movie-obj) url-slug)
      (assoc movie-obj :movie_tweets movie-tweets) 
      nil)))


(defn create-movie
  "Inserts movie data into db"
  [params]
  (println params)
  (let [db-movie (get-movie-rt (:movie_rt_id params))]
    (if (empty? db-movie)
      (insert movie (values params))
      db-movie)))


(defn format-date
  "format 2015-04-02"
  [date]
  (if (nil? date)
    nil
    (time/to-sql-date (time/parse-date date))))


(defn update-release-dates
  [rt-id dates]
  (korma/update movie 
          (set-fields {:movie_release_date_theater (format-date (:theater dates))
                       :movie_release_date_dvd (format-date (:dvd dates))})
          (where {:movie_rt_id (Integer. rt-id)})))


(defn get-movies [page page-limit]
  (let [offset-limit-params (pagination/offset-limit page page-limit)]
    (select movie
            (fields :movie_id
                    :movie_title
                    :movie_poster_thumbnail
                    :movie_rating
                    :movie_url_slug
                    :movie_hash_tag)
            (order [:movie_release_date_dvd] :DESC)
            (offset (:offset offset-limit-params))
            (limit (:limit offset-limit-params)))))


(defn search-movies
  [query]
  (exec-raw ["SELECT movie_id,movie_title,movie_poster_thumbnail,movie_rating,movie_url_slug,movie_hash_tag FROM movie WHERE movie_title ILIKE ? ORDER BY movie_release_date_dvd,movie_tomato_rating limit 100;" [(str "%" query "%")]] :results))


(defn get-movie-from-hash
  [hashes]
  (first 
   (select movie (where {:movie_hash_tag (first hashes)}))))

(defn delete-rating
  [movie-id twitter-user-id]
  (let [deleted-rating (delete movie_tweet 
                               (where {:movie_id movie-id
                                       :movie_tweet_user_id twitter-user-id}))]
    (if (> deleted-rating 0)
      true
      false)))


(defn rating-exists? 
  [tweet-params]
  (let [rating-data (select movie_tweet (where {:movie_tweet_twitter_id (:tweet-id tweet-params)}))]
    (if (empty? rating-data)
      false
      true)))


(defn update-user-rating
  [movie-data rating]
  (let [movie-ratings (select movie_tweet
                              (fields :movie_tweet_id :movie_tweet_rating)
                              (where {:movie_id (:movie_id movie-data)}))
        ratings-count (if (nil? (:movie_tomato_rating movie-data)) (count movie-ratings) (inc (count movie-ratings)) )
        total-rating (+ (reduce (fn [previous next] (+ previous (:movie_tweet_rating next))) 0 movie-ratings) (:movie_microcritix_rating movie-data))
        new-rating (round-places (/ total-rating ratings-count) 1)]
    (korma/update movie (set-fields {:movie_rating new-rating}) (where {:movie_id (:movie_id movie-data)}))))


(defn save-tweet
  [tweet-params]
  (println tweet-params)
  (let [movie-data (get-movie-from-hash (:hash-tags tweet-params))]
    (if (or (nil? movie-data) (> (:rating tweet-params) 10) (rating-exists? tweet-params))
      nil
      (do
        (delete-rating (:movie_id movie-data) (:user-id tweet-params))
        (insert movie_tweet (values {:movie_id (:movie_id movie-data)
                                     :movie_tweet_rating (:rating tweet-params)
                                     :movie_tweet_user_id (:user-id tweet-params)
                                     :movie_tweet_twitter_id (:tweet-id tweet-params)
                                     :movie_tweet_text (:tweet-text tweet-params)
                                     :movie_tweet_screen_name (:screen-name tweet-params)
                                     :movie_tweet_profile_image (:profile-image tweet-params)}))
        (update-user-rating movie-data (:rating tweet-params))))))
