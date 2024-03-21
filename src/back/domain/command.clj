(ns domain.command
  (:require [babashka.process :as p]
            [domain.explorer :refer [absolutize-path]]
            [domain.open-file :refer [open]]))

(defn run-string-as-cmd [cmd path  {:keys [root-dir-path] :as options}]
  (case cmd
    "open"      (open path options)
    (p/process [cmd (absolutize-path path root-dir-path)])))


(defn run [cmd-name path options]
  (cond
    (string? cmd-name) (run-string-as-cmd cmd-name path options)
    :else              (throw (ex-info "don't know how to run command" {:command-name cmd-name
                                                                        :path         path}))))