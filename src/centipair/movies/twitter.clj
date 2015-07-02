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
   )
  (:import
   (twitter.callbacks.protocols AsyncStreamingCallback)))




(def my-creds (make-oauth-creds "swlAjMKru1DW3VJKLpfSlg"
                                "Y99IXMobfvGyHp9xw7xzhido2BpLhHdIpHME0Ye7nHY"
                                "1319666455-DH4HFJTjnocXPmFj5XJix4GHJV4WgMBZzRD7cIX"
                                "Wr34fXd3nBGaVJMKmfDu8vtLe9rflg8syMBjdiphx8"))


(defn get-mentions []
  (statuses-mentions-timeline :oauth-creds my-creds :params {:count 1}))


(defn search [query]
  (search-tweets :oauth-creds my-creds :params {:q query :count 1}))


(defn search-mentions
  []
  (let [tweet (search "@devasiajoseph")]
    (println tweet)
    ))


(def temp (atom nil))

(defn parse-tweet
  [resp tweet]
  (if (not (= tweet "\r\n"))
    (println tweet)
    ))


(defn track-tweet [tracking]
  (statuses-filter :params {:track tracking}
                   :oauth-creds my-creds
                   :callbacks (AsyncStreamingCallback. 
                               (fn [resp tweet]
                                 (parse-tweet resp (str tweet)))
                               (comp println response-return-everything)
                               exception-print)))


(defn track-tweet-new []
  (let [w (io/writer "mary.txt")
      callback (AsyncStreamingCallback.
                 (fn [_resp payload]
                   (.write w (-> (str payload) json/read-json :text))
                   (.write w "\n"))
                 (fn [_resp]
                   (.close w))
                 (fn [_resp ex]
                   (.close w)
                   (.printStackTrace ex)))]
  (statuses-filter
    :params {:track "@devasiajoseph"}
    :oauth-creds my-creds
    :callbacks callback))
  )


(defn start-tweet-tracking
  []
  (schedule search-mentions (every 5 :second)))
