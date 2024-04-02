(ns walker
  (:require [clojure.walk :as w]))

(comment
  ;; explore walk usage.

  ;; Could be used to transform a nested map data structure (like the user config)

  ;; given the input map below (presumably obtains from JSON parse)
  (def input-1 {"port"  222
                "commands"  {"notepad"    {"instruction"  "notepad.exe"
                                           "description"  "run default notepad program"}

                             "vscode"     {"instruction" "c:\\program Files\\vscode.exe"}
                             "open-file"  "open"}})

  ;; we want to get this map
  (def ouput-1 {:port  222
                :commands  {"notepad"    {:instruction  "notepad.exe"
                                          :description  "run default notepad program"}

                            "vscode"     {:instruction "c:\\program Files\\vscode.exe"}
                            "open-file"  "open"}})

  ;; trying to walk the map

  ;; identity walk : output = input
  (w/walk identity identity input-1)

  (w/walk identity (fn [i]
                     (prn i)
                     (str "i- " i)) input-1)


  (defn build-command-instruction [instr]
    (cond->> instr
      (map? instr)  (w/walk #(vector (keyword (first %)) (second %)) identity)))

  (defn build-command-map [m]
    (w/walk (fn [[k v]]
              [k (build-command-instruction v)])  identity m))

  (w/walk  (fn [[k v]]
             (cond
               (= "commands" k) [(keyword k) (build-command-map v)]
               :else            [(keyword k) v])) identity input-1)
  
  ;;
  )