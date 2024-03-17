(ns server.handler.open-file
  (:require [server.response :as response]
            [domain.open-file :as open-file]))

(defn create
  "Create and return Request handler to open a file in the default system editor."
  [{:keys [root-dir-path]}]
  (fn [request]
    (let [file-path (get-in request [:params :path])]
      (response/ok (open-file/open file-path {:root-dir-path root-dir-path})))))