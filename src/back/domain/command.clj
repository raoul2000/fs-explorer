(ns domain.command
  (:require [babashka.process :as p]
            [domain.explorer.core :refer [absolutize-path]]
            [domain.open-file :refer [open]]
            [config :as cfg]))

(defn execute-action [action-m path config]
  (let [action-exec (cfg/action-exec action-m)]
    (case action-exec
      "open" (open path (cfg/root-dir-path config))
      (p/process [action-exec (absolutize-path path (cfg/root-dir-path config))]))))

(defn run-string-as-cmd [action-name path  {:keys [config] :as _options}]
  (if-let [action-m (cfg/find-action-by-name action-name (cfg/actions-definition config))]
    (execute-action  action-m  path config)
    (throw (ex-info "action not found" {:action-name action-name}))))

(defn run [cmd-name path options]
  (cond
    (string? cmd-name) (run-string-as-cmd cmd-name path options)
    :else              (throw (ex-info "don't know how to run command" {:command-name cmd-name
                                                                        :path         path}))))