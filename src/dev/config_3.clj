(ns config-3
  (:require [clojure.spec.alpha :as spec]
            [clojure.string :as str]))

(defn add-ns [ns-name k]
  (keyword ns-name (name k)))

(defn process-type-selectors [selectors-xs]
  (map #(into {} (map (fn [[k v]]
                        (vector (add-ns "selector" k) v)) %)) selectors-xs))

(defn process-type-actions [actions-xs]
  (map #(into {} (map (fn [[k v]]
                        (vector (add-ns "action" k) v)) %)) actions-xs))

(defn process-config-types [types-xs]
  (map #(into {} (map (fn [[k v]]
                        (vector (add-ns "type" k)
                                (case k
                                  :selectors (process-type-selectors v)
                                  :actions   (process-type-actions v)
                                  v))) %)) types-xs))

(defn process-config-actions [actions-xs]
  (map #(into {} (map (fn [[k v]]
                        (vector (add-ns "action" k) v)) %)) actions-xs))


(defn process-config [m]
  (into {} (map (fn [[k v]]
                  (vector (add-ns "config" k)
                          (case k
                            :types   (process-config-types v)
                            :actions (process-config-actions v)
                            v))) m)))

(comment
  ;; refactor / improve map ns adding to config map
  ;; use update-in to add namespace on deeply nested maps

  (def m1 {:browse-url "http://hostname"
           :actions [{:name "notepad"
                      :exec "notepad.exe"}
                     {:name "photoshop"
                      :exec "c:\\programs\\photoshop.exe"}]
           :types [{:name      "type1"
                    :selectors [{:starts-with "file"}
                                {:ends-with "txt"}]
                    :actions    [{:name "notepad"}]}]})

  ;; let's try something else


  (def m-ns (process-config {:k "v"
                             :k2 {:k22 "22"}
                             :actions [{:name "noptepad"
                                        :exec "notepad.exe"}]
                             :types [{:name "type1"
                                      :selectors [{:starts-with "something"}]}
                                     {:name "type2"
                                      :selectors [{:starts-with "file"}]}
                                     {:name "type3"
                                      :actions [{:name "notepad"}]
                                      :selectors [{:starts-with "file"}]}]}))

  (:config/k m-ns)
  (:type/name (first (:config/types m-ns)))

  (-> m-ns :config/types first :type/name)
  (-> m-ns :config/types second :type/selectors first :selector/starts-with)


  ;;
  )

 ;; and lets spec the config map 

  ;; some generic specs
(defn can-be-converted-to-url?
  "Returns TRUE if *s* can be converted into a java.net.URL object"
  [s]
  (try
    (new java.net.URL s)
    true
    (catch Throwable _t false)))

(spec/def :string/not-blank           (spec/and string? (complement str/blank?)))
(spec/def :coll/non-empty-string-list (spec/coll-of :string/not-blank :min-count 1))

(spec/def :action/name string?)
(spec/def :action/exec string?)
(spec/def :action/args  (spec/coll-of (spec/or :string  string?
                                               :number  number?
                                               :boolean boolean?)
                                      :min-count 1))

(spec/def :action/def (spec/keys :req [:action/name
                                       :action/exec]
                                 :opt [:action/args]))

(spec/def :selector/starts-with string?)
(spec/def :selector/ends-with   string?)
(spec/def :selector/equals      string?)
(spec/def :selector/def         (spec/keys :req [(or :selector/starts-with
                                                     :selector/ends-with
                                                     :selector/equals)]))

(spec/def :type/name  :string/not-blank)
(spec/def :type/selectors (spec/coll-of :selector/def :min-count 1))
(spec/def :type/actions   (spec/coll-of (spec/keys :req [:action/name]) :min-count 1))
(spec/def :type/def       (spec/keys :req [:type/name
                                           :type/selectors]
                                     :opt [:type/actions]))

(spec/def :config/server-port    (spec/and int? #(< 0 % 65353)))
(spec/def :config/root-dir-path  string?)
(spec/def :config/open-browser   boolean?)
(spec/def :config/browse-url     can-be-converted-to-url?)
(spec/def :config/types          (spec/coll-of :type/def   :min-count 1))
(spec/def :config/actions        (spec/coll-of :action/def :min-count 1))

(spec/def :config/map  (spec/keys :req [:config/server-port
                                        :config/root-dir-path
                                        :config/open-browser
                                        :config/browse-url]
                                  :opt [:config/types
                                        :config/actions]))

(spec/def :user-config/map  (spec/keys :opt [:config/server-port
                                             :config/root-dir-path
                                             :config/open-browser
                                             :config/browse-url
                                             :config/types
                                             :config/actions]))

(comment

  (def m-ns-2 (-> m-ns
                  (assoc :config/server-port 888)
                  (assoc :config/root-dir-path "path")
                  (assoc :config/open-browser true)
                  (assoc :config/browse-url "http://hostname")))

  (spec/valid? :config/map m-ns)
  (spec/explain :config/map m-ns)

  (spec/valid? :config/map m-ns-2)


  (spec/valid? :user-config/map m-ns)
  (spec/explain :user-config/map m-ns)
  ;;
  )
