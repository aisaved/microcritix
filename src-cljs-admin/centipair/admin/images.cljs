(ns centipair.admin.images
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [centipair.core.utilities.dom :as dom]
            [centipair.core.utilities.uuid :as uuid]
            [cljs.core.async :refer [put! chan <!]]))

(def  product-image-upload-channel (chan))

(defn toArray
  "To clojurescript array"
  [js-col]
  (-> (clj->js []) 
      (.-slice)
      (.call js-col)
      (js->clj)))


(defn filesize
  "Calculates file size in human readable format"
  [bytes]
  (if (= 0 bytes)
      "0 Bytes"
      (let [sizes ["Bytes" "KB" "MB" "GB" "TB"]
            index (js/parseInt (js/Math.floor (/ (js/Math.log bytes) (js/Math.log 1024))))
            value (js/Math.round (/ bytes (js/Math.pow 1024 index)) 2)
            suffix (nth sizes index)]
        (str value " " suffix))))


(defn progress-bar
  "progress bar component"
  [field key]
  [:div {:class "progress"}
   [:div {:class "progress-bar progress-bar-info"
          :role "progressbar"
          :aria-value-min "0"
          :aria-value-max "100"
          :style {:width (str (:progress (key (:file-data @field))) "%")}}]])


(defn update-progress
  "Updates progress bar"
  [field key progress]
  (let [all-files (:file-data @field)
        file-data (key (:file-data @field))
        new-file-data (assoc file-data :progress progress)
        ]
    (swap! field assoc :file-data (assoc all-files key new-file-data))))

(defn remove-file-for-upload
  "Remove a file from upload queue"
  [field key]
  (let [file-data (:file-data @field)]
    (swap! field assoc :file-data (dissoc file-data key))))

(defn upload-all-files
  "Triggers all uploads sequentially"
  [field]
  (let [key (first (first (seq (:file-data @field))))]
    (put! product-image-upload-channel [field key "all"])))

(defn upload-queue
  "Ajax upload"
  [pair]
  (let [field (first pair)
        key (second pair)
        file-data (key (:file-data @field))
        form-data (new js/FormData)
        xhr (new js/XMLHttpRequest)]
    (do
      (.append form-data "image" (:file file-data))
      (.open xhr "POST" (:post @field))
      (.setRequestHeader xhr "X-CSRF-Token" (dom/get-value "__anti-forgery-token"))
      ;;(.setRequestHeader xhr "Content-Type","multipart/form-data")
      (set! (.-onprogress xhr.upload)
            (fn [evt]
              (let [progress (* 100 (/ (.-loaded evt) (.-total evt)))]
                (update-progress field key progress))))
      (set! (.-onreadystatechange xhr)
            (fn []
              (if (and (= (.-readyState xhr) 4) (= (.-status xhr) 200))
                (do 
                  (remove-file-for-upload field key)
                  (if (and (= "all" (last pair)) (not (empty? (:file-data @field))))
                    (upload-all-files field))))))
      (.send xhr form-data))))


(defn upload-file
  "Single file upload"
  [field key]
  (put! product-image-upload-channel [field key]))


;;TODO: implemenmt this function
(defn delete-uploaded-file
  "Delete already uploaded file from server"
  [field key])

(defn image-preview
  "Image data preview table"
  [field file-data]
  (let [file (:file (second file-data))
        key (first file-data)
        name (.-name file)
        size (filesize (.-size file))
        type (.-type file)]
   [:tr {:key (str name "-row")}
    [:td {:key (str name "-name")} name]
    [:td {:key (str name "-size-" size)} size]
    [:td {:key (str name "-type-" type)} type]
    [:td {:key (str name "-status")} 
     (progress-bar field key)
     ;;(str "0/" size)
     ]
    [:td {:key (str name "-action-button")}
     [:i {:class "fa fa-upload"
          :style {:cursor "pointer"}
          :title "Upload this file"
          :on-click #(upload-file field key)}]
     " "
     [:i {:class "fa fa-times-circle"
          :style {:cursor "pointer"}
          :title "Remove this file"
          :on-click #(remove-file-for-upload field key)}]]]))


(defn upload-preview
  [field]
  [:table {:class "table table-hover"}
   [:tbody
    [:tr [:th "Name"] [:th "Size"] [:th "Type"] [:th "Upload status"] [:th]]
    (doall (map (partial image-preview field)  (seq (:file-data @field))))
    [:tr [:td {:col-span "5"} "Upload all " [:a {:class "fa fa-upload"
                                                 :style {:cursor "pointer" }
                                                 :on-click #(upload-all-files field)}]]]]])


(defn to-file-structure
  [previous next]
  (assoc previous (keyword (str (uuid/make-random-uuid))) {:file next :progress 0}))

(defn file-data
  [files]
  (let [file-list (toArray files)]
    (reduce to-file-structure {} file-list)))

(defn generate-preview
  "Event listener for geenrating image preview"
  [field files]
  ;;TODO: Append files instead of replacing? Think!
  (swap! field assoc :files (toArray files) :file-data (file-data files)))


(defn image-file-component
  "image upload component"
  [field]
  [:div [:form {:id (:form @field)
                :enc-type "multipart/form-data"}
         [:div {:class "form-group"}
          [:label {:for (:id @field)} "Select images"]
          [:input {:type "file" 
                   :multiple "multiple" 
                   :id (:id @field)
                   :name (:id @field)
                   :on-change #(generate-preview field (-> % .-target .-files) )
                   }]
          [:p {:class "help-block"} "Choose images for this product"]
          ]]
   (if (not (nil? (:files @field)))
     (upload-preview field))])


(defn init-product-image-upload-channel
  "initializing upload channel"
  []
  (go (while true
         (upload-queue (<! product-image-upload-channel)))))
