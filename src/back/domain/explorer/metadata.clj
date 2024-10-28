(ns domain.explorer.metadata
  (:require [babashka.fs :as fs]
            [clojure.data.json :as json]
            [clj-yaml.core :as yaml]
            [clojure.java.io :as io]))


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

(defn metadata-file-exists? [{metadata-file-path :path}]
  (when metadata-file-path
    (fs/regular-file? metadata-file-path)))


(defn read-metadata [metadata-format metadata-extension file-item]
  (let [candidates    (create-metadata-candidates file-item metadata-format metadata-extension)
        metadata-info (some metadata-file-exists? candidates)]

    (when metadata-info
      (try
        (let [metadata-reader (io/reader (fs/file (:path metadata-info)))]
          (case (:format metadata-info)
            :json (json/read         metadata-reader :key-fn keyword)
            :yaml (yaml/parse-stream metadata-reader)))
        (catch Exception e
          (throw (ex-info "failed to parse metadata file " {:metadata-info metadata-info
                                                            :msg          (.getMessage e)})))))))


