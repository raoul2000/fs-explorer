(ns server.handler.home
  (:require [clojure.java.io :as io]
            [server.response :as response]))

(defn create [_options]
  (fn home [_]
    (response/ok (slurp (io/resource "public/index.html")) {"Content-Type" "text/html"})))