(ns page.explore.view
  (:require [page.explore.subs :refer [<explore <loading? <current-dir]]
            [clojure.string :as s]
            [route.helper :refer [>navigate-to-explore create-url-explore]]
            [reagent.core :as r]
            [components.icon :refer [folder-icon file-icon]]
            ))

(defn view-item [item]
  [:tr  {:key (:file/path item)}
   [:td {:width "40px"} (if (:file/dir? item) folder-icon file-icon)]
   [:td
    [:a {:href (create-url-explore (:file/id item))} (:file/name item)]]])

(defn explorer-view []
  (let [loading? (<loading?)]
    [:div 
     (when-not loading?
       (let [list-items (<explore)]
         (if-not  (zero? (count list-items))
           [:table.table.is-hoverable.is-fullwidth.is-striped
            (map view-item list-items)]
           [:div "empty"])))]))

(defn toolbar []
  (let [path              (r/atom "")
        update-input-path #(reset! path (-> % .-target .-value))]
    (fn []
      [:div
       [:button
        {:on-click #(>navigate-to-explore @path)} "explore"]
       [:input {:type        "text"
                :placeholder "enter path"
                :value       @path
                :on-change   update-input-path}]])))

(defn page [params]
  (let [dir-path (<current-dir) #_(-> params :query :dir)]
    [:div
     [:div.title (str "Explorer - " dir-path)]
     [:br]
     [toolbar]
     [explorer-view]]))
