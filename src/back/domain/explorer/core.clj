(ns domain.explorer.core
  (:require [babashka.fs :as fs]
            [clojure.spec.alpha :as s]
            [model :as model]
            [domain.explorer.type :refer [infer-type]]
            [clojure.string :refer [blank? join]]))

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

(comment
  (fs-path->db-path "c:\\a" "c:\\a\\b")
  ;;
  )

(defn- read-file-content [file-path]
  {:model/content (slurp file-path)})

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

(defn- list-dir-content [dir-path root-dir-path]
  {:model/content (->> (fs/list-dir dir-path)
                       (map #(create-file-item % root-dir-path)))})


(comment
  (fs/file-name "c:\\tmp\\folder\\README.md")
  (fs/directory? "c:\\tmp\\folder\\README.md")
  (fs/directory? "c:\\tmp")
  (fs-path->db-path  "c:\\tmp" "c:\\tmp\\folder\\README.md")

  (create-file-item "c:\\tmp" "c:\\tmp\\README.md")
  ;;
  )

(defn absolutize-path
  "when *this-path* refers to a file or folder that is under *root-path* returns its absolute path 
   form as string otherwise throws. 
   Does not check if *this-path* refers to an existing file or folder."
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
  (update result :model/content (fn [file-xs]
                                  (map #(infer-type type-def-xs %) file-xs))))
;; explore  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn explore [fs-path {:keys [root-dir-path types] :as options}]
  {:post [(s/valid? :model/read-result %)]}
  (let [abs-path (absolutize-path (or fs-path "") root-dir-path)]

    (when-not (fs/exists? abs-path)
      (throw (ex-info "file not found" {:dir       fs-path
                                        :root-path root-dir-path
                                        :abs-path  abs-path})))

    (if (fs/regular-file? abs-path)
      (read-file-content abs-path)
      (->> (list-dir-content abs-path root-dir-path)
           (add-types        types)))))

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
