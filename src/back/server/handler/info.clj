(ns server.handler.info
  (:require [server.response :as response]))

(defn create
  "Create and return Request handler returning clojure and Java version "
  [_options]
  (fn [_request]
    (response/ok {:clojure-version (clojure-version)
                  :java-version    (System/getProperty "java.version")
                  :java-vm-version (System/getProperty "java.vm.version")
                  :java-vendor     (System/getProperty "java.vendor")})))
