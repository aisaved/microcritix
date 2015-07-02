(ns centipair.movies.jobs
  (:require [immutant.scheduling :refer :all]
            [centipair.movies.data :as movie-data]
            [taoensso.timbre :as timbre]))


(defn update-dvd-list []
  (timbre/info "Started updating dvd list")
  (do
    (movie-data/initialize-movie-channels)
    (movie-data/init-data 1 10 10)))



(defn start-scheduler
  []
  (schedule update-dvd-list
            (-> (in 5 :seconds)
                (every :hour))))
