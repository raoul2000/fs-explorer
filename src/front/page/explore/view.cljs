(ns page.explore.view
  (:require [clojure.spec.alpha :as spec]
            [page.explore.subs :refer [<loading? <sorted-explore <breadcrumbs]]
            [page.explore.event :refer [>run-command]]
            [db :refer [<config-actions]]
            [route.helper :refer [>navigate-to-explore create-url-explore]]
            [reagent.core :as r]
            [components.icon :refer [folder-icon file-icon home-icon]]
            [components.message :refer [warning-message]]
            [utils :refer [cancel-event]]))

(defn file-action [item config-actions]
  (tap> {:file-action config-actions
         :explain (spec/explain-data :user-config/actions config-actions)})
  (if-let   [{:keys [command]} (first (filter (fn [{:keys [selector]}]
                                                (= (:file/name item) selector)) config-actions))]
    ;; create anchor element's attributes for the selected action
    {:href  ""
     :on-click (fn [event]
                 (cancel-event event)
                 (js/console.log (str "running command " command))
                 (>run-command command (:file/id item)))}

    ;; default action on files : download inline
    {:href (str "/download?path=" (:file/path item) "&disposition=inline")
     :target (:file/name item)}))

(defn render-file [item config-actions]
  [:a  (file-action item config-actions) (:file/name item)])

(defn render-dir [item]
  [:a {:href (create-url-explore (:file/id item))} (:file/name item)])

(defn view-item [config-actions item]
  (let [is-dir (:file/dir? item)]
    [:tr  {:key (:file/path item)}
     [:td {:width "40px"} (if is-dir folder-icon file-icon)]
     [:td
      (if is-dir
        [render-dir item]
        [render-file item config-actions])]]))

(defn explorer-view []
  (let [loading? (<loading?)]
    [:div
     (when-not loading?
       (let [list-items     (<sorted-explore)
             config-actions (<config-actions)]
         (tap> config-actions)
         (if-not  (zero? (count list-items))
           [:table.table.is-hoverable.is-fullwidth
            [:tbody
             (map (partial view-item config-actions) list-items)]]
           [:div [warning-message "This folder is empty"]])))]))

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
              [:div.tags [:span.tag.is-medium.is-info.has-text-weight-bold (:name last-crumb)]]]]])))]]))


(defn page [params]
  [:div
   [breadcrumbs]
   #_[toolbar]
   [explorer-view]])
