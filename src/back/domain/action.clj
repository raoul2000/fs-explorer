(ns domain.action
  (:require [babashka.process :as p]
            [domain.explorer.core :refer [absolutize-path create-file-item]]
            [domain.explorer.type :as type]
            [domain.open-file :refer [open]]
            [config :as cfg]
            [clojure.string :as s]
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

(defn run-string-as-cmd [type-action-m path config]
  (if-let [merged-action-m (merge (cfg/find-action  (cfg/action-name type-action-m) config)
                                  type-action-m)]
    (execute-action  merged-action-m  path config)
    (throw (ex-info "action not found for given type" {:action-name (cfg/action-name type-action-m)}))))


(defn run [action-name path {:keys [config] :as _options}]
  (when (s/blank? action-name)
    (throw (ex-info "don't know how to run action" {:action-name action-name})))

  (let [root-dir-path (cfg/root-dir-path config)
        abs-path      (absolutize-path path root-dir-path)]

    (when-not (fs/exists? abs-path)
      (throw (ex-info "file not found" {:path          path
                                        :absolute-path abs-path})))

    (if-let [type-m (type/select-type (create-file-item abs-path root-dir-path) (cfg/types-definition config))]
      (if-let [type-action-m (cfg/find-type-action (cfg/type-name type-m) action-name config)]
        (run-string-as-cmd type-action-m abs-path config)
        (throw (ex-info "action name not found for type" {:action-name action-name
                                                          :type-name   (cfg/type-name type-m)})))
      (throw (ex-info "no type found for file" {:path path
                                                :absolute-path abs-path})))))

