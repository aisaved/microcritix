(ns centipair.movies.dvd
  (:require [reagent.core :as reagent]
            [centipair.core.ui :as ui]
            [centipair.core.components.notifier :as notifier]
            [centipair.core.utilities.validators :as v]
            [centipair.core.utilities.ajax :as ajax]))



(def movie-list-data (reagent/atom {:page 0
                                    :limit 30
                                    :data []
                                    :load-more "block"}))

(def row-count (atom 0))


(defn prepare-movie-data
  [data-list]
  (let [partition-data (partition-all 6 data-list)
        added-data (concat (:data @movie-list-data) partition-data)]
    (swap! movie-list-data assoc :data added-data)))

(defn fetch-movie-list
  []
  (ajax/get-json 
   "/api/1/movies/dvd"
   {:page (:page @movie-list-data)
    :limit (:limit @movie-list-data)}
   (fn [response]
     (prepare-movie-data response))))

(defn load-more
  []
  (let [next-page (inc (:page @movie-list-data))]
    (swap! movie-list-data assoc :page next-page))
  (fetch-movie-list))


(defn movie-block
  [movie]
  [:div {:class "col-md-2 text-center"
         :id (str "movie-cell-" (:movie_id movie))
         :key (str "movie-cell-" (:movie_id movie))}
    [:div {:class "text-center"
           :id (str "movie-poster-container" (:movie_id movie))
           :key (str "movie-poster-container" (:movie_id movie))}
     [:a {:href (str "/movie/" (:movie_id movie) "/" (:movie_url_slug movie))
          :id (str "movie-link-" (:movie_id movie))
          :key (str "movie-link-" (:movie_id movie))
          }
      [:img {:src (:movie_poster_thumbnail movie)
             :width "180px"
             :height "245px"
             ;;:class "img-responsive"
             :id (str "movie-poster-" (:movie_id movie))
             :key (str "movie-poster-" (:movie_id movie))}]]]
   [:div {:class "text-center"
          :id (str "movie-info-container-" (:movie_id movie))
          :key (str "movie-infor-container-" (:movie_id movie))}
    [:h6 {:id (str "movie-title-" (:movie_id movie))
          :key (str "movie-title-" (:movie_id movie))}
     (:movie_title movie)]
    [:h6 {:id (str "movie-hash-" (:movie_id movie))
          :key (str "movie-hash-" (:movie_id movie))}
     [:i {:class "fa fa-twitter"
          :key (str "movie-hash-icon" (:movie_id movie))}]
     [:a {:target "_blank"
          :href (str "https://twitter.com/intent/tweet"
                     "?text=@microcritix " (:movie_title movie)
                     "&hashtags=" (str (:movie_hash_tag movie) ",rating"))}
      (:movie_hash_tag movie)]]
    [:span
     {:id (str "movie-rating-" (:movie_id movie))
      :key (str "movie-rating-" (:movie_id movie))}
     (if (v/has-value? (:movie_rating movie))
       (str "Rating: " (:movie_rating movie))
       "")]
    ]])


(defn movie-row
  [row]
  (swap! row-count inc)
  [:div {:class "row"
         :id (str "dvd-row-" @row-count)
         :key (str "dvd-row-" @row-count)}
   (doall (map movie-block row))])


(def search-query (reagent/atom {:id "search-query" :value ""}))


(defn do-search []
  (ajax/get-json 
   "/api/1/movies/search"
   {:q (:value @search-query)}
   (fn [response]
     (if (empty? response)
       (do
         (notifier/notify 422 "Search results not found"))
       (do 
         (swap! movie-list-data assoc :data [])
         (swap! movie-list-data assoc :load-more "none")
         (prepare-movie-data response))))))


(defn search-form []
  [:div {:class "form-inline"}
   [:div {:class "form-group"}
    [:input {:type "text"
             :class "form-control"
             :placeholder "Search movies"
             :value (:value @search-query)
             :on-change #(swap! search-query assoc :value (-> % .-target .-value))
             
            }]]
   
   [:button {:class "btn btn-default"
               :type "button"
               :on-click #(do-search)} "Search"]])



(defn movie-list
  []
  [:div
   (doall (map movie-row (:data @movie-list-data)))
   [:button {:class "btn btn-lg btn-block"
             :type "button"
             :on-click #(load-more)
             :style {:display (:load-more @movie-list-data)}} "Load more"]])





(defn render-movie-list []
  (swap! movie-list-data assoc :load-more "block")
  (fetch-movie-list)
  (ui/render movie-list "movie-list")
  (ui/render search-form "search-form"))



(defn render-tweets
  []
  
  )
