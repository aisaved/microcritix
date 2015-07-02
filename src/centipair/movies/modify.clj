(ns centipair.movies.modify
  (:use korma.core
        korma.db
        centipair.core.db.connection)
  (:require [centipair.movies.models :as movie-models]
            [slugger.core :as slugger]))


(defn update-movie-url-slug
  []
  (doseq [each (select movie-models/movie)]
    (update movie-models/movie 
            (set-fields {:movie_url_slug (slugger/->slug (:movie_title each))})
            (where {:movie_id (:movie_id each)}))))
