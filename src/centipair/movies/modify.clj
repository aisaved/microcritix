(ns centipair.movies.modify
  (:use korma.db
        centipair.core.db.connection)
  (:require [centipair.movies.models :as movie-models]
            [slugger.core :as slugger]
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


(defn update-movie-url-slug
  []
  (doseq [each (select movie-models/movie)]
    (korma/update movie-models/movie 
                  (set-fields {:movie_url_slug (slugger/->slug (:movie_title each))})
                  (where {:movie_id (:movie_id each)}))))




(defn update-movie-hash-tag
  []
  (doseq [each (select movie-models/movie)]
    (korma/update movie-models/movie 
                  (set-fields {:movie_hash_tag (movie-models/title-hash (:movie_title each))})
                  (where {:movie_id (:movie_id each)}))))
