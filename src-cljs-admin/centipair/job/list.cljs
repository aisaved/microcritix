(ns centipair.job.list
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [cljs.core.async :refer [put! chan <!]]
   [reagent.core :as reagent]
   [centipair.core.utilities.ajax :as ajax]
   [centipair.core.ui :as ui]
   [centipair.core.components.table :refer [data-table
                                            generate-table-rows
                                            per-page]]))

(def job-list-channel (chan))


(defn job-list-headers
  []
  [:tr
   [:th "Title"]
   [:th "Expires"]
   [:th "Action"]])

(def job-data (reagent/atom {:page 0
                             :id "job-table"
                             :url "jobs"
                             :total 0
                             :rows [:tr [:td "Loading"]]
                             :headers (job-list-headers)
                             :create {:entity "job"} 
                             :delete {:action (fn [] (.log js/console "delete"))}
                             :site-settings-id nil
                             :id-field "job_id"
                             :search false}))

(defn delete-job [job-id]
  (ajax/delete
   (str "/api/private/job/"job-id)
   (fn [response]
     (put! job-list-channel (:page @job-data)))))

(defn job-row
  [row-data]
  [:tr {:key (str "table-row-" ((keyword (:id-field @job-data)) row-data))}
   [:td {:key (str "table-column-1-" ((keyword (:id-field @job-data)) row-data))} (:job_title row-data)]
   [:td {:key (str "table-column-2-" ((keyword (:id-field @job-data)) row-data))} (str (:job_expiry_date row-data))]
   [:td {:key (str "table-column-3-" ((keyword (:id-field @job-data)) row-data))}
    [:a {:href (str "#/job/edit/" (:job_id row-data))
         :key (str "row-edit-link-" ((keyword (:id-field @job-data)) row-data))} "Edit "]
    [:a {:href "javascript:void(0)" :on-click (partial delete-job (:job_id row-data))
        :key (str "row-delete-link-" ((keyword (:id-field @job-data)) row-data)) } " Delete"]]])

(defn create-job-data-list []
  (data-table job-data))


(defn render-job-list []
  (ui/render create-job-data-list "content"))


(defn fetch-job-list [page-number]
  (swap! job-data assoc :page (js/parseInt page-number))
  (ajax/get-json
   "/api/private/jobs"
   {:page (:page @job-data)
   :per per-page}
   (fn [response]
     (generate-table-rows response job-data job-row))))



(defn init-job-list-channel
  []
  (go (while true
         (fetch-job-list (<! job-list-channel)))))
