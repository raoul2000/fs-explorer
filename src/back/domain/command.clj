(ns domain.command
  (:require [domain.open-file :refer [open]]))

(defn run [cmd-name path options]
  (case cmd-name
    "open"      (open path options)
    
    (throw (ex-info "unkown command name" {:command-name cmd-name
                                           :path         path}))))