(ns domain.explorer.core
  (:require [babashka.fs :as fs]
            [clojure.spec.alpha :as s]
            [model :as model]
            [domain.explorer.type :refer [infer-type ignored-type]]
            [domain.explorer.metadata :refer [read-metadata]]
            [clojure.string :refer [blank? join ends-with?]]))

;; utils  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn normalize-path-separator
  "Force path separator to '/' if not already defined as the platform path separator
   otherwise return *path* unchanged."
  [^String path]
  (if (= fs/path-separator "/")
    path
    (->> path
         fs/components
         (join "/"))))

(defn fs-path->db-path
  "Given absolute path *fs-path* returns a db path relatively to *root-path*.
   
   A db-path is a relative path with char '/' as path separator. 
   
   Example (Windows):
   ```clojure
   (fs-path->db-path \"c:\\folder1\\db\" \"c:\\folder1\\db\\folder2\\folder3\")
   => \"folder2/folder3\"
   ```
   "
  [root-path fs-path]
  (->> (fs/relativize root-path fs-path)
       normalize-path-separator))

(defn create-file-item
  "Returns a map describing the file at *abs-path* given the *root-dir-path*. Returns `nil` if
   *abs-path* does not exist."
  [abs-path root-dir-path]
  {:post [(or (s/valid? :file/info %)
              (nil? %))]}
  (when (fs/exists? abs-path)
    {:file/name (fs/file-name abs-path)
     :file/dir? (fs/directory? abs-path)
     :file/path (str abs-path)
     :file/id   (fs-path->db-path root-dir-path abs-path)}))

(defn- list-dir-content
  "creates and returns a seq of file items stored under the given *dir-path* and recursively. 
   When metadata is enabled, files ending with metadata extension are ignored."
  [dir-path root-dir-path {metadata-enabled :metadata/enable
                           metadata-file-ext :metadata/file-extension}]
  {:model/content (->> (fs/list-dir dir-path)
                       (map #(create-file-item % root-dir-path))
                       (filter #(or (not metadata-enabled)
                                    (not (ends-with? (:file/path %) (str "." metadata-file-ext))))))})

(defn absolutize-path
  "when *this-path* refers to a file or folder that is under *root-path* returns its absolute path 
   form as string otherwise throws. 

   Does not check if *this-path* exists."
  [^String this-path ^String root-path]
  (if (blank? this-path)
    (fs/normalize root-path)
    (let [abs-path         (if (fs/relative? this-path)
                             (fs/normalize (fs/path root-path this-path))
                             (fs/normalize this-path))

          first-component (->> (fs/relativize root-path abs-path)
                               (fs/components)
                               first
                               str)]
      (when (= ".." first-component)
        (throw (ex-info "path outside root-path" {:dir       this-path
                                                  :root-path root-path})))
      (str abs-path))))

;; type ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn add-types [type-def-xs result]
  (if-not type-def-xs
    result
    (update result :model/content (fn [file-xs]
                                    (map #(infer-type type-def-xs %) file-xs)))))

(defn remove-ignored-types [type-def-xs result]
  (if-not type-def-xs
    result
    (update result :model/content (fn [file-xs]
                                    (remove #(ignored-type type-def-xs %) file-xs)))))

;; meta ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn add-meta [with-meta {:keys [:metadata/enable]
                           :as metadata-conf} result]
  (if (and with-meta
           enable)
    (update result :model/content (fn [file-xs]
                                    (map #(read-metadata metadata-conf %) file-xs)))
    result))

;; explore  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn explore [fs-path {:keys [root-dir-path
                               types
                               metadata]}]
  {:post [(s/valid? :model/read-result %)]}
  (let [abs-path (absolutize-path (or fs-path "") root-dir-path)
        with-meta true]

    (when-not (fs/exists? abs-path)
      (throw (ex-info "directory not found" {:dir       fs-path
                                             :root-path root-dir-path
                                             :abs-path  abs-path})))

    (when (fs/regular-file? abs-path)
      (throw (ex-info "not a directory" {:dir       fs-path
                                         :root-path root-dir-path
                                         :abs-path  abs-path})))

    (->> (list-dir-content abs-path root-dir-path metadata)
         (add-types            types)
         (remove-ignored-types types)
         (add-meta             with-meta metadata))))

;; index ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def dir-index-type  "dir")
(def file-index-type "file")
(def default-index-type dir-index-type)
(def valid-index-types #{dir-index-type file-index-type})

(defn build-dir-index [root-dir-path]
  (let [path-coll (volatile! [])
        abs-path   (fs/absolutize root-dir-path)]
    (when (and (fs/exists? abs-path)
               (fs/directory? abs-path))
      (fs/walk-file-tree abs-path {:pre-visit-dir (fn [path _attr]
                                                    (when-not (= path abs-path)
                                                      (vswap! path-coll conj path))
                                                    :continue)})
      (->> @path-coll
           (map str)
           (map (fn [path]
                  (fs-path->db-path root-dir-path path)))
           (map normalize-path-separator)))))

;; TODO: index on dir should NOT return dir with an 'ingored' type

(defn index
  "Builds and returns the list of all files or folders under *root-dir-path*"
  [index-type {:keys [root-dir-path] :as options}]
  (let [idx-type (or index-type default-index-type)]

    (when-not (valid-index-types  idx-type)
      (throw (ex-info "invalid index type" {:type   index-type
                                            :domain valid-index-types})))
    (when (= idx-type file-index-type)
      (throw (ex-info "not implemented type" {:type idx-type})))

    {:type   idx-type
     :index (condp = idx-type
              dir-index-type (build-dir-index root-dir-path))}))
