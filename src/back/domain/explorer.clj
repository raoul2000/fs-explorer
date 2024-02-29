(ns domain.explorer
  (:require [babashka.fs :as fs]
            [clojure.spec.alpha :as s]
            [model :as model]
            [clojure.string :refer [blank? join]]))

(defn fs-path->db-path
  "Converts *fs-path* an OS file system absolute path into a db path. The db *root-path*
   is an absolute path to the DB root folder.
   
   Example:
   ```clojure
   (fs-path->db-path \"c:\\folder1\\db\" \"c:\\folder1\\db\\folder2\\folder3\")
   => \"folder2/folder3\"
   ```
   "
  [root-path fs-path]
  (->> (fs/relativize root-path fs-path)
       fs/components
       (join "/")))

(defn- read-file-content [file-path]
  {:model/content (slurp file-path)})

(defn- list-dir-content [dir-path root-dir-path]
  {:model/content (->> (fs/list-dir dir-path)
                       (map (fn [file]
                              {:file/name (fs/file-name file)
                               :file/dir? (fs/directory? file)
                               :file/path (str file)
                               :file/id   (fs-path->db-path root-dir-path file)})))})

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

(defn explore [fs-path {:keys [root-dir-path] :as options}]
  {:post [(s/valid? :model/read-result %)]}
  (let [abs-path (absolutize-path (or fs-path "") root-dir-path)]

    (when-not (fs/exists? abs-path)
      (throw (ex-info "file not found" {:dir       fs-path
                                        :root-path root-dir-path
                                        :abs-path  abs-path})))

    (if (fs/regular-file? abs-path)
      (read-file-content abs-path)
      (list-dir-content abs-path root-dir-path))))

(comment
  ;; if path is relative then
  ;;    it is relative to root-dir-path
  ;;    absolutize it
  ;;  endif
  ;;  if abs-path is not under root-dir-path then
  ;;     error
  ;;  endif

  (fs/relative? "c:/a/b")

  (fs/path "c:/a/b"  "/c/s")

  (->> (fs/relativize "c:/a" "c:/a/b/c")
       (fs/components)
       (join "/"))

  (fs/components "b/C")



  (defn normalize-path [dir root-path]
    (if (blank? dir)
      (fs/normalize root-path)
      (let [abs-dir (if (fs/relative? dir)
                      (fs/normalize (fs/path root-path dir))
                      (fs/normalize dir))
            first-component (->> (fs/relativize root-path abs-dir)
                                 (fs/components)
                                 first
                                 str)]
        (when (= ".." first-component)
          (throw (ex-info "invalid dir" {:dir dir
                                         :root-path root-path})))
        (fs/normalize abs-dir))))

  (normalize-path "b/c" "c:/a")
  (normalize-path "/" "c:/a")
  (normalize-path "b/c/../../d" "c:/a")
  (normalize-path "b/c/../../../d" "c:/a")
  (normalize-path "../../a/b" "c:/a")
  (normalize-path "../../d/b" "c:/a")

  (fs/normalize "a/b/../c")


  (fs/path "c:/tmp" "../lof")
  (s/valid? :model/read-result (explore "tmp" {:root-dir-path "c:/tmp"}))
  (s/explain-data :model/read-result (explore "tmp"  {:root-dir-path "c:"}))
  (->> (fs/list-dir "c:\\tmp")
       (map str))

  (def root "/a/b/c")

  (def p1 "d")

  (defn is-inside? [dir base-dir]
    (if-not (fs/absolute? dir)
      true
      (let [rel-dir (fs/relativize dir base-dir)])))

  (fs/unixify (fs/normalize (fs/path "/a/b/c" "../../..")))
  (fs/relativize (fs/path "c:/tmp")  (fs/path "c:/tmp/barcode/d"))
  (fs/relativize (fs/path "c:/tmp")  (fs/path "c:/a/b"))
  (fs/real-path "c:/tmp/../") ;; => c:/tmp
  (fs/normalize "c:/tmp/../") ;; => c:/tmp
  (fs/normalize "/a/b/c/../d")

  (fs/relativize "/a/b/d" "/a/b/../c/../d")
  (fs/normalize "/a/../../a")

  (fs/relativize "c:/" "")
  (fs/relativize "/a/b/c" "/a/b/d")
  (fs/normalize "/a/b/./c/../d")
  (fs/components "/a/b/c")
  (fs/components "../a/b/c")
  ;;
  )