(ns compress
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
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


  ;;
  )