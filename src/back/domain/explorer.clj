(ns domain.explorer
  (:require [babashka.fs :as fs]
            [clojure.spec.alpha :as s]
            [model :as model]))

(defn- read-file-content [file-path]
  {:model/content (slurp file-path)})

(defn- list-dir-content [dir-path]
  {:model/content (->> (fs/list-dir dir-path)
                       (map (fn [file]
                              {:file/name (fs/file-name file)
                               :file/dir? (fs/directory? file)
                               :file/path (str file)})))})

(defn explore [fs-path {:keys [root-dir-path] :as options}]
  {:post [(s/valid? :model/read-result %)]}
  (let [abs-path (fs/path root-dir-path (or fs-path "/"))]
    (if (fs/regular-file? abs-path)
      (read-file-content abs-path)
      (list-dir-content abs-path))))

(comment
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
      (let [rel-dir (fs/relativize dir base-dir)]
        
        )
      )
    )

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
  ;;
  )