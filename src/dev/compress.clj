(ns compress
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [babashka.fs :as fs])
  (:import java.util.zip.GZIPInputStream
           java.util.zip.GZIPOutputStream
           java.util.zip.ZipEntry
           java.util.zip.ZipOutputStream))

;; compress file and folder

(defn gzip
  "Writes the contents of input to output, compressed.
  input: something which can be copied from by io/copy.
  output: something which can be opend by io/output-stream.
      The bytes written to the resulting stream will be gzip compressed."
  [input output & opts]
  (with-open [output (-> output io/output-stream GZIPOutputStream.)]
    (apply io/copy input output opts)))


(defn zip-folder
  "p input path, z output zip"
  [p z]
  (with-open [zip (ZipOutputStream. (io/output-stream z))]
    (doseq [f (file-seq (io/file p)) :when (.isFile f)]
      (.putNextEntry zip (ZipEntry. (str/replace-first (.getPath f) p "")))
      (io/copy f zip)
      (.closeEntry zip)))
  (io/file z))
(comment

  (io/copy "test/fixture/node-script/prog1.js" "c:\\tmp\\out.zip")
  (io/copy  (io/file "test/fixture/node-script/prog1.js") (io/file "c:\\tmp\\out.js"))

  ;; works ok but the gzip archive cannot be decompressed with 7zip 
  (gzip (io/file "test/fixture/node-script/prog1.js") (io/file "c:\\tmp\\out.zip"))

  ;; works ok but the zip file contains folder  "test/fixture/node-script"
  (zip-folder "test/fixture/node-script" "c:\\tmp\\out-2.zip")

  ;; using babashka fs ?
  
  (fs/zip "c:\\tmp\\out-2.zip" ["test/fixture/node-script"] 
          {:root "test/fixture/node-script" })
  
  ; ok but using absolute path ?
  (def entry (str (fs/absolutize "test/fixture/node-script")))
  (fs/zip "c:\\tmp\\out-3.zip" [entry]) ;; 🤔 nope. Only relative entries

  (def abs-entry (str (fs/absolutize "test/fixture/node-script")))
  (str (fs/relativize (fs/cwd) abs-entry))

  ;; what if cwd and entry path are on distinc drvies in Windows
  (str (fs/relativize "d:\\" abs-entry)) ;; 😣 - fail : 'other has different root'

  (fs/zip "c:\\tmp\\out-4.zip" ["file://e:/286.jpg"])
  

  ;;
  )