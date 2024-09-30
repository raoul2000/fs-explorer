(ns domain.command
  (:require [babashka.process :as p]
            [domain.explorer.core :refer [absolutize-path]]
            [domain.open-file :refer [open]]
            [config :as cfg])
  (:import [java.net URLEncoder]))

(defn execute-action [action-m path config]
  (let [action-exec   (cfg/action-exec action-m)
        abs-path      (absolutize-path path (cfg/root-dir-path config))
        result-m      {:action action-m}]
    (case action-exec
      "open"      (assoc result-m :result (open path (cfg/root-dir-path config)))
      "download"  (-> result-m
                      (assoc :redirect (str "/download?path="
                                            (URLEncoder/encode path)
                                            "&disposition=attachment"))
                      (assoc :target "_self"))

      "view"      (-> result-m
                      (assoc  :redirect (str "/download?path="
                                             (URLEncoder/encode path)
                                             "&disposition=inline"))
                      (assoc :target "_blank"))
      (assoc result-m :process (do
                                 (p/process [action-exec abs-path])
                                 true)))))

(defn run-string-as-cmd [action-name path  {:keys [config] :as _options}]
  (if-let [action-m (cfg/find-action-by-name action-name (cfg/actions-definition config))]
    (execute-action  action-m  path config)
    (throw (ex-info "action not found" {:action-name action-name}))))

(defn run [cmd-name path options]
  (if (string? cmd-name)
    (run-string-as-cmd cmd-name path options)
    (throw (ex-info "don't know how to run command" {:command-name cmd-name
                                                     :path         path}))))