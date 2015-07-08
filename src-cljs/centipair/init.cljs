(ns centipair.init
  (:require [centipair.core.components.notifier :as notifier]
            [centipair.core.user.forms :as user-forms]
            [centipair.core.csrf :as csrf]
            [centipair.movies.dvd :refer [render-movie-list]]))



(defn ^:export init! []
  (do
    (notifier/render-notifier-component)
    ;;(render-movie-list)
    ;;(csrf/fetch-csrf-token)
    ;;(render-components)
    ))
