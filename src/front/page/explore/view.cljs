(ns page.explore.view
  (:require [page.explore.event :refer [>explore]]
            [page.explore.subs :refer [<explore <loading? <current-dir]]
            [clojure.string :as s]
            [route.helper :refer [>navigate-to-explore]]
            [reagent.core :as r]))

(defn view-item [item]
  [:tr  {:key (:file/path item)}
   [:td {:width "40px"} (if (:file/dir? item) "dir" "file")]
   [:td
    [:a {:on-click #(>navigate-to-explore (:file/path item))} (:file/name item)]]])

(defn explorer-view [dir-path]
  (let [loading? (<loading?)]
    [:div "explorer view"
     [:br]
     (when-not loading?
       (let [current-dir (<current-dir)]
         (if-not (= current-dir dir-path)
           (>explore dir-path)
           (let [list-items (<explore)]
             (if-not  (zero? (count list-items))
               [:table.table.is-hoverable.is-fullwidth.is-striped
                (map view-item list-items)]
               [:div "empty"])))))]))


(defn toolbar []
  (let [path              (r/atom "")
        update-input-path #(reset! path (-> % .-target .-value))]
    (fn []
      [:div
       [:button
        {:on-click #(>navigate-to-explore @path)} "explore"]
       [:input {:type "text"
                :placeholder "enter path"
                :value @path
                :on-change update-input-path}]])))

(defn page [params]
  (let [dir-path (-> params :path :path)]
    #_(tap> params)
    [:div
     [:div.title (str "Explorer - " dir-path)]
     [:br]
     [toolbar]
     [explorer-view dir-path]]))
