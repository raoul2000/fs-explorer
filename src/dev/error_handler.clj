(ns error-handler
  (:require [portal.api :as p]))


(comment



  (try
    (p/clear)
    
    (/ 1 0)
    #_(throw (ex-info "error message" {:info "extra info"}))

    #_(throw (new java.lang.ArithmeticException "msg"))

    (catch Exception ex
      (tap> {:ex-data    (ex-data ex)
             :ex-message (ex-message ex)
             :ex-cause   (ex-cause ex)})
      (print "error")))

  ;;
  )