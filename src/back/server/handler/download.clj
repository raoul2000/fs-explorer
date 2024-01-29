(ns server.handler.download
  (:require [babashka.fs :as fs]
            [server.response :as response]))


(defn create [_options]
  {:name ::download-file-handler
   :enter (fn [context]
            (assoc context :response
                   (response/ok (fs/file (fs/path (fs/cwd) "test" "back" "sample" "sample.pdf"))
                              ;; set Content-Disposition header to force download.
                              ;; Replace 'attachment' with 'inline' to ask the browser to show the
                              ;; file content
                                {"Content-Disposition" "attachment; filename=\"filename.pdf\""}
                              ;; Note that the Content-Type header is set by the ring-mw/file-info interceptor
                              ;; (see route)
                              ;; Other option is to force the Content-Type header :
                              ;; "Content-Type" "image/jpg"
                                )))})
