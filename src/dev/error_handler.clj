(ns error-handler
  (:require [portal.api :as p]))



(defn ex-msg-chain [ex delimiter]
  (->> ex
       (iterate ex-cause)
       (take-while some?)
       (mapv ex-message)
       (interpose delimiter)
       (apply str)))
  
(comment



  (try
    (p/clear)

    #_(/ 1 0)
    #_(throw (ex-info "error message" {:info "extra info"}))

    #_(throw (new java.lang.ArithmeticException "msg"))
    (throw (ex-info "error message" {:info "the info"} (new java.lang.ArithmeticException "msg")))

    (catch Exception ex
      (tap> {:ex-data    (ex-data ex)
             :ex-message (ex-message ex)
             :ex-cause   (ex-cause ex)})

      (tap> (ex-message (ex-cause ex)))))

  (try

    (try

      (p/clear)
      (throw (ex-info "root error message"
                      {:info "root  info"} (new java.lang.ArithmeticException "division by zero")))

      (catch Exception ex1
        (throw (ex-info "there was an error"
                        {:info "info"} ex1))))

    (catch Exception ex
      (tap> {:ex-data    (ex-data ex)
             :ex-message (ex-message ex)
             :ex-cause   (ex-cause ex)})

      (tap>  (ex-msg-chain ex " - "))))

  ;;
  )