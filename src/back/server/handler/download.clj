(ns server.handler.download
  (:require [babashka.fs :as fs]
            [server.response :as response]
            [domain.explorer.core :as explorer]
            [config :as config]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:import java.util.zip.ZipEntry
           java.util.zip.ZipOutputStream))

(defn zip-folder
  "p input path, z output zip"
  [dir-path zip-file-path]
  (with-open [zip (ZipOutputStream. (io/output-stream zip-file-path))]
    (doseq [f (file-seq (io/file dir-path)) :when (.isFile f)]
      (.putNextEntry zip (ZipEntry.
                          (->> (str/replace-first (.getPath f) dir-path "")
                               fs/components
                               (str/join "/"))))
      (io/copy f zip)
      (.closeEntry zip)))
  (io/file zip-file-path))

(def valid-dispositions #{"inline" "attachment"})
(def default-disposition "attachment")


(defn download-file [file-path disposition root-dir-path]
  (cond
    (nil? file-path)
    (response/error-BAD_REQUEST  (response/error-body "missing 'path' param"))

    (not (valid-dispositions disposition))
    (response/error-BAD_REQUEST  (response/error-body "invalid disposition value"
                                                      {:disposition  disposition
                                                       :domain       valid-dispositions}))

    :else
    (let [abs-path (explorer/absolutize-path file-path root-dir-path)]
      (cond
        (not (fs/exists? abs-path))
        (response/error-BAD_REQUEST  (response/error-body "file not found"
                                                          {:file  abs-path}))

        (fs/directory? abs-path)
        (response/ok (->> (fs/create-temp-file {:prefix "fsx_" :suffix ".zip"})
                          str
                          (zip-folder abs-path))
                     {"Content-Disposition" (format "attachment; filename=\"%s.zip\""  (fs/file-name abs-path))})

        (fs/regular-file? abs-path)
        (response/ok  (fs/file abs-path)
                      {"Content-Disposition" (format "%s; filename=\"%s\"" disposition (fs/file-name abs-path))
                                      ;; Note that the Content-Type header is set by the ring-mw/file-info interceptor
                                      ;; (see route)
                                      ;; Other option is to force the Content-Type header :
                                      ;; "Content-Type" "image/jpg" 
                       })

        :else
        (response/error-SERVER_ERROR (response/error-body "invalid file type"
                                                          {:file abs-path}))))))

(defn create [{:keys [config] :as _route-config}]
  (fn [request]
    (let [file-path      (get-in request [:params :path])
          disposition    (get-in request [:params :disposition] default-disposition)
          root-dir-path  (config/root-dir-path config)]
      (download-file file-path disposition root-dir-path))))


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
