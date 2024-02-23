(ns page.explore.view
  (:require [re-frame.core :as re-frame]
            [page.explore.event :refer [>explore]]
            [page.explore.subs :refer [<explore <loading?]]
            [clojure.string :as s]
            [reagent.core :as r]))

(defn view-item [item]
  [:tr  {:key (:file/path item)}
   [:td (if (:file/dir? item) "dir" "file")]
   [:td
    [:a {:on-click #(>explore (:file/path item))} (:file/name item)]]])

(defn explorer-view []
  [:div "explorer view"]
  (if-let [list-items (<explore)]
    [:table.table.is-hoverable.is-fullwidth.is-striped
     (map view-item list-items)]
    [:div "empty"]))

(defn toolbar []
  (let [loading?          (<loading?)
        path              (r/atom "")
        update-input-path #(reset! path (-> % .-target .-value))]
    (fn []
      [:div
       [:button
        {:disabled (or loading? (s/blank? @path))
         :on-click #(>explore @path)}
        (if loading? "loading..." "explore")]
       [:input {:type "text"
                :placeholder "enter path"
                :value @path
                :on-change update-input-path}]])))

(defn page [params]
  (tap> params)
  [:div
   [:div.title "Explorer"]
   [:br]
   [:div (-> params :path :path)]
   [toolbar]
   [explorer-view]])