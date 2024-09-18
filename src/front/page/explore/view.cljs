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

(defn eval-selector-rule [[rule-name rule-arg] s]
  (cond
    (= rule-name :action.selector/equals)  (= rule-arg s)
    (= rule-name :action.selector/match)   (re-find (re-pattern rule-arg) s)
    :else (throw (ex-info "unkown selector rule type" {:selector-rule-name rule-name
                                                       :selector-rule-arg  rule-arg}))))

(defn selector-match [{id :file/id} selector-val]
  #_(tap> {:selector-match true
         :id id
         :sel selector-val
         :map? (map? selector-val)
         :filter (when (map? selector-val)
                   (filter #(eval-selector-rule % id) selector-val))})
  (cond
    (string? selector-val)   (= selector-val id)
    (map?    selector-val)   (filter #(eval-selector-rule % id) selector-val)
    :else                    (throw (ex-info "failed to apply selector" {:selector selector-val}))))

(comment
  (selector-match #:file{:id "filename.txt"}
                  #:action.selector{:dummy "dummy-value"})

  (selector-match #:file{:id "filename.txt"}
                  #:action.selector{:equals "filename.txt"})
  (selector-match #:file{:id "filename.txt"}
                  #:action.selector{:dummy "dummy-value"})
  (map identity #:action.selector{:dummy "dummy-value"})

  (filter (fn [n]
            (when (= 2 n)
              (throw (ex-info "boum" {:n n})))
            true) [1 2 3])
  ;;
  )

(defn find-matching-command [config-actions item]
  (js/console.log config-actions)
  (->> config-actions
       (take-while #(selector-match item (:selector %)))
       (first)
       :command))


(comment
  (def cfg-actions [#:user-config{:selector "readme.txt"
                                  :command "CMD1"}])

  (try
    #_(find-matching-command cfg-actions #:file{:id "readme.txt"})
    (find-matching-command cfg-actions #:file{:id "filename.txt"})
    (catch ExceptionInfo ex (ex-data ex)))


  ;;
  )

(defn file-action [item config-actions]
  (if-let   [{:keys [command]} (find-matching-command config-actions item)
             #_(first (filter (fn [{:keys [selector]}]
                                (= (:file/name item) selector)) config-actions))]
    ;; create anchor element's attributes for the selected action
    {:href  ""
     :on-click (fn [event]
                 (cancel-event event)
                 (js/console.log (str "running command " command))
                 (>run-command command (:id item)))}

    ;; default action on files : download inline
    {:href (str "/download?path=" (:path item) "&disposition=inline")
     :target (:name item)}))

(defn render-file [item config-actions]
  [:a  (file-action item config-actions) (:name item)])

(defn render-dir [item]
  [:a {:href (create-url-explore (:id item))} (:name item)])

(defn view-item [config-actions item]
  (let [is-dir (:dir? item)]
    [:tr  {:key (:path item)}
     [:td {:width "40px"} (if is-dir folder-icon file-icon)]
     [:td
      (if is-dir
        [render-dir item]
        [render-file item config-actions])]
     [:td (:type item)]]))

(defn explorer-view []
  (let [loading? (<loading?)]
    [:div
     (when-not loading?
       (let [list-items     (<sorted-explore)
             config-actions (<config-actions)]
         (tap> {:config-actions config-actions
                :list-items list-items})
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
