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
          {:root "test/fixture/node-script"})

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

(comment
  ;; fs/zip doesn't accept relative path.
  ;; let's try to write our own zip function

  (defn zip-folder
    "p input path, z output zip"
    [dir-path zip-file-path]
    (with-open [zip (ZipOutputStream. (io/output-stream zip-file-path))]
      (doseq [f (file-seq (io/file dir-path)) :when (.isFile f)]
        (.putNextEntry zip (ZipEntry.
                            (->> (str/replace-first (.getPath f) dir-path "")
                                 fs/components
                                 (str/join "/"))))
        (io/copy f zip)
        (.closeEntry zip)))
    (io/file zip-file-path))

  (fs/create-temp-file)
  (fs/create-temp-file {:prefix "fs_"})
  (io/output-stream (str (fs/create-temp-file {:prefix "fs_"})))
  
  (fs/directory? "f:\\toto")
  (fs/exists? "f:\\toto")
  (zip-folder "e:\\tmp" "c:\\tmp\\out-4.zip")
  (zip-folder "c:\\tmp\\bck" "c:\\tmp\\out-4.zip")

  (def tmp-zip (str (fs/create-temp-file {:prefix "fs_" :suffix ".zip"})))

  (print tmp-zip)
  (zip-folder "c:\\tmp\\bck" tmp-zip)
  (io/copy (io/file tmp-zip) (io/file "c:\\tmp\\final.zip"))

  (->> (str/replace-first "e:\\tmp\\img.jpg" "e:\\tmp" "")
       #_(str/replace-first "e:\\tmp\\res\\img.jpg" "e:\\tmp" "")
       fs/components
       (str/join "/"))
  (file-seq (io/file "e:\\tmp"))

  (def root-dir "e:\\tmp")
  (doseq [f (file-seq (io/file root-dir))
          :when (.isFile f)]
    (prn (fs/relativize root-dir f)))

  (fs/unixify (str (fs/relativize "e:\\tmp" "e:\\tmp\\res\\file.txt")))
  ;; nope : adds drive letter


  (fs/relativize "e:\\tmp" "e:\\tmp\\res\\file.txt")

  (->> (fs/relativize "e:\\tmp" "e:\\tmp\\res\\file.txt")
       fs/components
       (str/join "/"))

  (fs/unixify "c:\\tmp")

  (zip-folder "test/fixture/node-script" "c:\\tmp\\out-2.zip")
  ;;
  )