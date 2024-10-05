(ns domain.command
  (:require [babashka.process :as p]
            [domain.explorer.core :refer [absolutize-path]]
            [domain.open-file :refer [open]]
            [config :as cfg]
            [babashka.fs :as fs])
  (:import [java.net URLEncoder]))


(comment

  (p/shell {:continue  true
            :out       :string
            :err       :string
            :extra-env {"MY_VAR" "__MY_VALUE"}}
           "node" (fs/absolutize "test/back/process/prog1.js") "__arg1" "__arg2" "__arg2 with space__")

  (def run-shell (partial p/shell {:continue  true
                                   :out       :string
                                   :err       :string
                                   :extra-env {"MY_VAR" "__MY_VALUE"}}))

  (apply run-shell ["node" (fs/absolutize "test/back/process/prog1.js") "__arg1" "__arg2" "__arg2 with space__"])

  (defn run-shell2 [args-xs]
    (apply (partial p/shell {:continue  true
                             :out       :string
                             :err       :string
                             :extra-env {"MY_VAR" "__MY_VALUE"}}) args-xs))

  (run-shell2 ["node" (fs/absolutize "test/back/process/prog1.js") "__arg1" "__arg2" "__arg2 with space__"])
  ;;
  )

(def abs-path-placeholder "FILE_PATH")

(defn create-args-vec
  "Creates and returns a vector suitable to be passed to the p/shell or p/process
   function.
   
   All occurences of the *abs-path-placholder* are replaced with actual value.
   "
  [action-m abs-path]
  (let [exec        (cfg/action-exec action-m)
        args        (cfg/action-args action-m)
        interpolate (partial map #(if (= abs-path-placeholder %) abs-path %))
        args-v      (cond
                      (coll? args)   (vec args)
                      (string? args) (vector args)
                      :else          [])]
    (->> (if (first (filter #{abs-path-placeholder} args-v))
           (interpolate args-v)
           (conj args-v abs-path))
         (into [exec]))))

(defn run-process [action-m abs-path]
  (let [arg-xs  (create-args-vec action-m abs-path)
        options {:continue  true ;; don't throw when exit code not zero (error)
                 :out       :string
                 :err       :string
                 :extra-env {"MY_VAR" "__MY_VALUE"
                             "MY Other Var" "my Other Value"}}
        result  (if (:action/wait action-m)
                  (apply (partial p/shell   options) arg-xs)
                  (apply (partial p/process options) arg-xs))]

    (tap> {:result result
           :arg-xs arg-xs
           :wait (:action/wait action-m)})

    {:cmd  (:cmd result)
     :out  (when (:action/wait action-m) (:out result))
     :err  (when (:action/wait action-m) (:err result))
     :exit (:exit result)}))

(comment

  (run-process  #:action{:name "Run Notepad"
                         :exec "notepad"
                         :args "arg1"
                         :wait true} "")

  (run-process  #:action{:name "My NodeJS"
                         :exec "node"
                         :args "C:\\dev\\ws\\lab\\fs-explorer\\test\\back\\process\\prog1.js"
                         :wait true} "")
  ;;
  )

(defn execute-action [action-m path config]
  (let [abs-path      (absolutize-path path (cfg/root-dir-path config))
        result-m      {:action action-m}]
    (case (cfg/action-exec action-m)
      "open"      (assoc result-m :result (open path (cfg/root-dir-path config)))
      "download"  (-> result-m
                      (assoc :redirect (str "/download?path="
                                            (URLEncoder/encode path)
                                            "&disposition=attachment"))
                      (assoc :target "_self"))

      "view"      (-> result-m
                      (assoc  :redirect (str "/download?path="
                                             (URLEncoder/encode path)
                                             "&disposition=inline"))
                      (assoc :target "_blank"))
      (assoc result-m :process (do
                                 (tap> {:action-m action-m
                                        :abs-path abs-path})
                                 (run-process  action-m abs-path))))))

;; TODO: based on the path, search the matching type and on this type, search the action
;; This would remove the need of the type param.
(defn run-string-as-cmd [action-name path type  {:keys [config] :as _options}]
  (if-let [action-m (merge (cfg/find-action       action-name config)
                           (cfg/find-type-action  type action-name config))]
    (execute-action  action-m  path config)
    (throw (ex-info "action not found for given type" {:action-name action-name
                                                       :type        type}))))

(defn run [cmd-name path type options]
  (if (string? cmd-name)
    (run-string-as-cmd cmd-name path type options)
    (throw (ex-info "don't know how to run command" {:command-name cmd-name
                                                     :path         path}))))

(comment
  (merge {:a 1 :b 2 :d 44} {:a 11 :c 33})
  (merge #:action{:a 1 :b 2 :d 44} #:dummy{:a 11 :c 33})
  (merge {:a 1 :b 2} nil)
  (merge  nil {:a 1 :b 2})
  (merge  nil nil)
  ;;
  )