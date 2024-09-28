(ns domain.process
  (:require [babashka.process :as p]
            [babashka.fs :as fs]
            [clojure.string :as s]
            [clojure.java.io :as io]))

(comment

  ;; wait until command terminates
  (p/shell "notepad.exe")

  ;; does NOT wait until command terminates (can open multiple notepad.exe)
  (p/process "notepad.exe")
  (p/process ["c:\\program files\\notepad++\\notepad++.exe" (fs/absolutize "test/back/process/prog1.js")] )
  ;; to wait process termination : 
  (-> (p/process "notepad.exe") deref)

  ;; the map returned by deref contains exit code (but not only)
  (-> (p/process "notepad.exe") deref :exit)


  ;; store output into a string
  (:out (p/shell {:out :string} "node" "-v"))

  ;; store output into a string : same as previous cmd. p/check block until the end and
  ;; throws when exit code is not zero
  (-> (p/process {:out :string} "node" "-v") p/check :out)


  ;; calls a nodejs prog
  (:out (p/shell {:out :string} "node" (fs/absolutize "test/back/process/prog1.js")))

  ;; calls a nodejs prog pass CLI arguments 
  (-> (:out (p/shell {:out :string} "node" (fs/absolutize "test/back/process/prog1.js") "arg1 for script" "arg 2 for script"))
      (s/split-lines))

  ;; calls a nodejs prog pass environment variables
  (-> (:out (p/shell {:out :string :extra-env {"MY_VAR" "MY_VALUE"}} "node" (fs/absolutize "test/back/process/prog1.js")))
      (s/split-lines))

  ;; use p/process instead of p/shell, to read process output while it is still running
  ;; for example : long-proc-with-output.js output 10 seconds with delay and then end
  
  (def proc-with-output (p/process "node" (fs/absolutize "test/back/process/long-proc-with-output.js")))
  
  ;; read max 15 times from proc output
  (with-open [rdr (io/reader (:out proc-with-output))]
    (binding [*in* rdr]
      (loop [max 15]
        (when-let [line (read-line)]
          (tap> line)
          (println :line line)
          (when (pos? max)
            (recur (dec max)))))))
  
  ;; kill proc
  (p/destroy-tree proc-with-output)


  ;;
  )