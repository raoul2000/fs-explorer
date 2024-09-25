(ns page.config.view
  (:require [page.config.subs :refer [<config]]
            [components.message :refer [error-message info-message]]))


(defn field-no-browser [value]
  [:div.field
   [:div.control
    [:label.checkbox
     [:input {:type "checkbox" :readOnly true :checked (not value)}] " Open default browser on startup "]]])

(defn field-browse-url [value]
  [:div.field
   [:label.label "Initial URL"]
   [:div.control
    [:input.input {:type "text", :placeholder "Text input" :readOnly true
                   :value value}]
    [:p.help "URL to open on startup. Ignored when 'open default browser on startup' is disabled"]]])

(defn field-server-port [value]
  [:div.field
   [:label.label "Server Port"]
   [:div.control
    [:input.input {:type "text", :placeholder "Text input" :readOnly true
                   :value value}]]])

(defn field-root-dir-path [value]
  [:div.field
   [:label.label "Root Directory Path"]
   [:div.control
    [:input.input {:type "text", :placeholder "Text input" :readOnly true
                   :value value}]
    [:p.help "Path to the root folder"]]])

(defn user-config [user-config]
  [:div
   [info-message "The configuration displayed in this page is for information purposes only.
                  To change the configuration please refer to the documentation."]
   [field-server-port   (:port user-config)]
   [field-no-browser    (:open-browser? user-config)]
   [field-browse-url    (:browse-url user-config)]
   [field-root-dir-path (:root-dir-path user-config)]])

(defn page []
  (let [config (<config)]
    [:div.title "Configuration"
     [:hr]
     (if (seq config)
       [user-config config]
       [:div
        [error-message "The configuration could not be loaded."]])]))