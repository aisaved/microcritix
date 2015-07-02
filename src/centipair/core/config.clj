(ns centipair.core.config
  (:require [clojure.edn :as edn]))



(defn load-db-config
  "Loads db config from congif.edn file in classpath
  keys: :db-name :db-username :db-password
  "
  []
  (let [db-details (clojure.edn/read-string (slurp "config.edn"))]
    {:db (:db-name db-details)
     :user (:db-user db-details)
     :password (:db-password db-details)}))


;;db config
(defonce db-config (load-db-config))
