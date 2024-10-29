(ns domain.explorer.metadata
  "Manage metadata 
   
   Metadata for a given file item, may be stored as a regular file following this naming conventions :

   ```
   [file path token][.format token].<meta extension token>
   ```

   Where : 
   - [file path token]` : 
   
   "
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

(defn create-metadata-candidates 
  "Returns a map describing all possible metadata file info for the given *file-m* descriptor.
   
   Keys :
   - `:path` : the absolute path to the metadata file
   - `:format` : metadata format. One of `:json` or `:yaml`
   "
  [file-m  metadata-format metadata-extension]
  (let [file-path (file-path-token file-m)]
    (->> (format-token metadata-format)
         (map (fn [token]
                (assoc token :path (format "%s%s.%s"
                                           file-path
                                           (:token token)
                                           metadata-extension)))))))

(defn file-exists? [{metadata-file-path :path}]
  (when metadata-file-path
    (fs/regular-file? metadata-file-path)))


(defn parse-metadata [metadata-info]
  (with-open [metadata-reader (io/reader (fs/file (:path metadata-info)))]
    (tap> {:metadata-info       metadata-info})
    (try
      (case (:format metadata-info)
        :json (json/read         metadata-reader :key-fn keyword)
        :yaml (yaml/parse-stream metadata-reader)
        (throw (ex-info "internal error : unsupported metadata format" {:metadata-format (:format metadata-info)})))
      (catch Exception e
        (throw (ex-info "failed to parse metadata file " {:metadata-info metadata-info
                                                          :msg          (.getMessage e)}))))))

(defn read-metadata [metadata-format metadata-extension file-item]
  (let [metadata-candidates (create-metadata-candidates file-item metadata-format metadata-extension)]
    (if-let [metadata-info (first (filter file-exists? metadata-candidates))]
      (assoc file-item :file/metadata (parse-metadata metadata-info))
      file-item)))


