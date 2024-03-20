(ns domain.command
  (:require [domain.open-file :refer [open]]))

(defn run [cmd-name path options]
  (case cmd-name
    "open"      (open path options)
    :else       true))