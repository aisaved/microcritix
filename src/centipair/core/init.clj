(ns centipair.core.init
  (:require [centipair.core.channels :as core-channels]))


(defn init-system []
  (do
    (core-channels/init-core-channels)))
