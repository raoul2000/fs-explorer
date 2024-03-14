(ns page.explore.view
  (:require [page.explore.subs :refer [<loading? <sorted-explore <breadcrumbs]]
            [clojure.string :as s]
            [route.helper :refer [>navigate-to-explore create-url-explore]]
            [reagent.core :as r]
            [components.icon :refer [folder-icon file-icon]]
            [components.message :refer [message]]
            [components.icon :refer [home-icon]]))

(defn view-item [item]
  [:tr  {:key (:file/path item)}
   [:td {:width "40px"} (if (:file/dir? item) folder-icon file-icon)]
   [:td
    [:a {:href (create-url-explore (:file/id item))} (:file/name item)]]])

(defn explorer-view []
  (let [loading? (<loading?)]
    [:div
     (when-not loading?
       (let [list-items (<sorted-explore)]
         (if-not  (zero? (count list-items))
           [:table.table.is-hoverable.is-fullwidth
            [:tbody (map view-item list-items)]]
           [:div [message "This folder is empty"]])))]))

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

(defn breadcrumbs []
  (let [crumbs     (<breadcrumbs)]
    [:div.breadcrumb.has-arrow-separator.is-medium
     [:ul
      [:li {:key  "home"} [:a {:href (create-url-explore "")} home-icon]]

      (when (seq crumbs)
        (concat

         (when-let [head (butlast crumbs)]
           (->> head
                (map (fn [{:keys [name path]}]
                       [:li  {:key path}
                        [:a {:href (create-url-explore path)} name]]))))

         (when-let [last-crumb (last crumbs)]
           [[:li.is-active {:key  (:path last-crumb)}
             [:a {:href (create-url-explore (:path last-crumb))}
              [:div.tags [:span.tag.is-medium.is-info (:name last-crumb)]]]]])))]]))


(defn page [params]
  [:div
   [breadcrumbs]
   #_[toolbar]
   [explorer-view]])
