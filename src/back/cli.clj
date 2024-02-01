(ns cli
  "Command Line Arguments module"
  (:require [clojure.string    :refer [join]]
            [clojure.tools.cli :refer [parse-opts]]))

(def default-server-port 8890)

(def cli-options
  [["-p" "--port PORT" "Port number"
    :default  default-server-port
    :validate [#(re-matches #"[0-9]+" %)  "Must be a number"
               #(< 0 (Integer/parseInt %) 0x10000) "Must be a number between 0 and 65536"]]

   ["-n" "--no-browser" "Do not open browser on startup"]
   ;; A boolean option defaulting to nil
   ["-h" "--help"]])

(defn usage [summary-opts]
  (->> [""
        "usage: java -jar fs-explorer-X.X.X.jar [options]"
        ""
        "Options:"
        summary-opts
        ""]
       (join \newline)))

(defn help-option? [parsed-opts]
  (get-in parsed-opts [:options :help]))

(defn parse-cli-options [args]
  (parse-opts args cli-options :in-order true))

(defn show-errors [errors]
  (->> errors
       (map (partial str "\t- "))
       (join \newline)
       (str "Error:\n")))


(defn validate-cli-options [args]
  (let [{:keys [options arguments error summary]} (parse-opts args cli-options :in-order true)]
    (cond
      (:help options)   {:exit-message (usage summary)
                         :ok?          true}
      error             {:exit-message (usage summary)
                         :ok?          false}
      
      :else             {:exit-message (usage summary)
                         :ok?          true})))



(comment
  (validate-cli-options ["-h"])
  (parse-cli-options ["--port" "8000"])
  (parse-cli-options ["--port" "99999"])
  (parse-cli-options ["--port" "X99999"])
  ;;
  )