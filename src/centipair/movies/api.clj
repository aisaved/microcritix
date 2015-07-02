(ns centipair.movies.api
  (:use compojure.core)
   (:require [liberator.core :refer [resource defresource]]
             [centipair.core.contrib.response :as response]
             [centipair.movies.models :as movie-models]))



(defresource api-dvd-releases [& [source]]
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [context]
               (response/liberator-json-response
                (movie-models/get-movies 
                 (get-in context [:request :params :page])
                 (get-in context [:request :params :limit])))))


(defresource api-search [& [source]]
  :available-media-types ["application/json"]
  :allowed-methods [:get]
  :handle-ok (fn [context]
               (response/liberator-json-response
                (movie-models/search-movies
                 (get-in context [:request :params :q])))))


(defroutes api-movie-routes
  (GET "/api/1/movies/dvd" [] (api-dvd-releases))
  (GET "/api/1/movies/search" [] (api-search)))
