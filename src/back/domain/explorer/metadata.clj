(ns domain.explorer.metadata
  (:require [babashka.fs :as fs]))


(def metadata-format-info {"mixed" [{:token ".json", :format :json}
                                    {:token ".yaml"  :format :yaml}]
                           "json"  [{:token "",      :format :json}]
                           "yaml"  [{:token "",      :format :yaml}]})


(defn format-token [metadata-format]
  (if-let [format-info (get metadata-format-info metadata-format)]
    format-info
    (throw (ex-info "invalid metadata format" {:metadata-format metadata-format}))))

(defn file-path-token [file-m]
  (let [file-path (:file/path file-m)]
    (format "%s%s" file-path (if (:file/dir? file-m) fs/file-separator ""))))

(defn create-metadata-candidates [file-m  metadata-format metadata-extension]
  (let [file-path (file-path-token file-m)]
    (->> (format-token metadata-format)
         (map (fn [token]
                (assoc token :path (format "%s%s.%s"
                                           file-path
                                           (:token token)
                                           metadata-extension)))))))
(comment


  (defn metadata-file-exists? [{metadata-file-path :path}]
    (when metadata-file-path
      (and
       (fs/regular-file? metadata-file-path)
       (fs/exists? metadata-file-path))))

  (metadata-file-exists? {:path "c:\\tmp\\file.txt"})


  (some metadata-file-exists? (create-metadata-candidates #:file{:path "c:\\tmp\\file.txt"
                                                                 :dir false}
                                                          "mixed"
                                                          "meta"))


;;
  )


(defn metadata-file-info [file-item metadata-format metadata-extension]

  (let [format-part (if (= metadata-format "mixed") [".json" ".yaml"] [])]))

(comment
  (def metadata-format "mixed")

  ;;
  )


(defn read-metadata [metadata-format metadata-extension file-item]
  (let [metadata-asb-path (str (:file/path file-item))]
    (assoc file-item :metadata {:sample "value"})))
