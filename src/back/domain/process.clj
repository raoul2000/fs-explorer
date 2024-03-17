(ns domain.process
  (:require [babashka.process :as p]
            [babashka.fs :as fs]))

(comment

  ;; wait until command terminates
  (p/shell "notepad.exe" )

  ;; does NOT wait until command terminates (can open multiple notepad.exe)
  (p/process "notepad.exe")
  ;; to wait process termination : 
  (-> (p/process "notepad.exe") deref :exit)


  

  ;; store output into a string
  (:out (p/shell {:out :string} "node" "-v"))

  ;; store output into a string : same as previous cmd. p/check block until the end and
  ;; throws when exit code is not zero
  (-> (p/process {:out :string} "node" "-v") p/check :out)

  
  ;; calls a nodejs prog
  (:out (p/shell {:out :string} "node" (fs/absolutize "test/back/process/prog1.js")))


  ;;
  )