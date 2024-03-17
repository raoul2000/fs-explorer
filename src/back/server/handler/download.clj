(ns server.handler.download
  (:require [babashka.fs :as fs]
            [server.response :as response]
            [domain.explorer :as explorer]))

(def valid-dispositions #{"inline" "attachment"})
(def default-disposition "attachment")

(defn create [{:keys [root-dir-path]}]
  (fn [request]
    (let [param-file        (get-in request [:params :path])
          param-disposition (get-in request [:params :disposition] default-disposition)]
      (cond
        (nil? param-file)
        (response/error-BAD_REQUEST  (response/error-body "missing 'path' param"))

        (not (valid-dispositions param-disposition))
        (response/error-BAD_REQUEST  (response/error-body "invalid disposition value"
                                                          {:disposition  param-disposition
                                                           :domain       valid-dispositions}))

        :else
        (let [abs-path (explorer/absolutize-path param-file root-dir-path)]
          (if-not (fs/regular-file? abs-path)
            (response/error-SERVER_ERROR (response/error-body "file not found"
                                                              {:file abs-path}))
            (response/ok (fs/file abs-path)
                         {"Content-Disposition" (format "%s; filename=\"%s\"" param-disposition (fs/file-name abs-path))
                          ;; Note that the Content-Type header is set by the ring-mw/file-info interceptor
                          ;; (see route)
                          ;; Other option is to force the Content-Type header :
                          ;; "Content-Type" "image/jpg" 
                          })))))))


(defn create_2 [{:keys [root-dir-path]}]
  (fn [request]
    (if-let [file-path (get-in request [:params :dir])]
      (let [abs-path    (explorer/absolutize-path file-path root-dir-path)
            disposition (get-in request [:params :disposition] "attachment")]

        (when-not (#{"inline" "attachment"} disposition)
          (throw (ex-info "invalid disposition value" {:disposition disposition})))

        (if (fs/regular-file? abs-path)
          (response/ok (fs/file abs-path)
                       {"Content-Disposition" (format "%s; filename=\"%s\"" disposition (fs/file-name abs-path))
                        ;; Note that the Content-Type header is set by the ring-mw/file-info interceptor
                        ;; (see route)
                        ;; Other option is to force the Content-Type header :
                        ;; "Content-Type" "image/jpg" 
                        })
          (response/error-SERVER_ERROR {:msg "file not found"
                                        :file abs-path})))
      (response/error-NOT_FOUND {:msg "missing dir param"}))))

(defn create_1
  [{:keys [root-dir-path]}]
  {:name ::download-handler
   :enter (fn [context]
            (if-let [dir-path (get-in  context [:request :params :dir])]
              (let [abs-path (explorer/absolutize-path dir-path root-dir-path)]
                (assoc context :response (if (fs/regular-file? abs-path)
                                           (response/ok (fs/file abs-path)

                                                        {"Content-Disposition" (format "attachment; filename=\"%s\"" (fs/file-name abs-path))})
                                           (response/error-SERVER_ERROR {:msg      "file not found..."
                                                                         :file-path abs-path})))


                #_(if (fs/regular-file? abs-path)
                    (assoc context :response
                           (response/ok (fs/file abs-path)
                                                               ;; set Content-Disposition header to force download.
                                                               ;; Replace 'attachment' with 'inline' to ask the browser to show the
                                                               ;; file content
                                        {"Content-Disposition" (format "attachment; filename=\"%s\"" (fs/file-name abs-path))}
                                                               ;; Note that the Content-Type header is set by the ring-mw/file-info interceptor
                                                               ;; (see route)
                                                               ;; Other option is to force the Content-Type header :
                                                               ;; "Content-Type" "image/jpg"
                                        ))
                    (response/error-SERVER_ERROR {:msg "file not found..."
                                                  :file-path abs-path})
                    #_(throw (ex-info "file not found" {:file-path abs-path}))))
              (assoc context :response (response/error-SERVER_ERROR {:msg "missing file path"}))
              #_(throw (ex-info "missing file path" {}))))}



  #_{:name ::download-file-handler
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
