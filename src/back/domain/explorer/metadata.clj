(ns domain.explorer.metadata
  (:require [babashka.fs :as fs]))


(defn format-token [metadata-format]
  (case metadata-format
    "mixed"   [{:token ".json", :format "json"} {:token ".yaml" :format "yaml"}]
    "json"    [{:token "", :format "json"}]
    "yaml"    [{:token "", :format "yaml"}]
    (throw (ex-info "invalid metadata format" {:metadata-format metadata-format}))))

(comment

  (format-token "json")
  (format-token "mixed")
  (format-token "yaml")
  (format-token "???")

  ;;
  )

(defn file-path-token [file-m]
  (let [file-path (:file/path file-m)]
    (format "%s%s" file-path (if (:file/dir? file-m) fs/file-separator ""))))

(comment

  (fs/file-separator)

  (file-path-token #:file{:dir? true
                          :path "c:\\tmp\\folder"})
  (file-path-token #:file{:dir? false
                          :path "c:\\tmp\\file.txt"})

  ;;
  )

(comment
  (->> (format-token "mixed")
       (map #(format "file.txt%s%s" % ".meta")))



  ;;
  )

(defn create-abs-path [content-path metadata-format metadata-extension]
  ;; TODO: implement me
  content-path)

(defn metadata-file-info [file-item metadata-format metadata-extension]

  (let [format-part (if (= metadata-format "mixed") [".json" ".yaml"] [])]))

(comment
  (def metadata-format "mixed")

  ;;
  )


(defn read-metadata [metadata-format metadata-extension file-item]
  (let [metadata-asb-path (str (:file/path file-item))]
    (assoc file-item :metadata {:sample "value"})))
