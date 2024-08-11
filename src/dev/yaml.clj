(ns yaml
  (:require [clj-yaml.core :as yaml]
            [babashka.fs :as fs]
            [clojure.java.io :as io]))


(comment

  (slurp "./test/back/fixtures/file-1.yaml")

  (yaml/parse-stream (io/reader "./test/back/fixtures/file-1.yaml"))
  


  ;;
  )



