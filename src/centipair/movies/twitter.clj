(ns centipair.movies.twitter
  (:use
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.callbacks.handlers]
   [twitter.api.streaming]
   [twitter.api.restful]
   [immutant.scheduling :refer :all]
   )
  (:require
   [http.async.client :as ac]
   [cheshire.core :refer [parse-string]]
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [centipair.movies.models :refer [save-tweet]]
   )
  (:import
   (twitter.callbacks.protocols AsyncStreamingCallback)))




(def my-creds (make-oauth-creds "swlAjMKru1DW3VJKLpfSlg"
                                "Y99IXMobfvGyHp9xw7xzhido2BpLhHdIpHME0Ye7nHY"
                                "1319666455-DH4HFJTjnocXPmFj5XJix4GHJV4WgMBZzRD7cIX"
                                "Wr34fXd3nBGaVJMKmfDu8vtLe9rflg8syMBjdiphx8"))


(defn hash-tag-parser
  [tweet-text]
  (filter
   (fn [each] (not (= each "#rating")))
   (into [] (re-seq  #"\B#\w*[a-zA-Z]+\w*" tweet-text))))


(defn get-rating-value
  [rating-text]
  (if (nil? rating-text)
    0
    (let [rating-value (bigdec (clojure.string/replace rating-text #"#rating +" ""))]
      (if (> rating-value 10)
        (/ rating-value 10)
        rating-value))))

(defn rating-parser
  [tweet-text]
  (let [rating-text (first (re-find #"\B#rating +\d+(\.\d{1,2})?" tweet-text))]
    (get-rating-value rating-text)))




(defn process-tweet
  [tweet]
  (let [tweet-text (:text tweet)
        tweet-params {:tweet-text (:text tweet)
                      :hash-tags (hash-tag-parser tweet-text)
                      :rating (rating-parser tweet-text)
                      :tweet-id (:id tweet)
                      :user-id (:id (:user tweet))}]
    (println tweet-params)
    (save-tweet tweet-params)))

(defn get-mentions []
  (statuses-mentions-timeline :oauth-creds my-creds :params {:count 1}))


(defn search [query]
  (search-tweets :oauth-creds my-creds :params {:q query :count 200}))


(defn search-mentions
  []
  (let [tweets (search "@microcritix")]
    (doseq [tweet (:statuses (:body tweets))] 
      (process-tweet tweet))))


(defn start-tweet-tracking
  []
  (schedule search-mentions (every 5 :second)))
