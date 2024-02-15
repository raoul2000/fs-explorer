(ns model
  (:require [clojure.spec.alpha :as s]))


(s/def ::path string?)
(s/def ::content (s/coll-of ::path))
(s/def ::read-result (s/keys :req [::content]))


(comment
  
  (s/valid? ::path "eee")
  (s/valid? ::path  true)

  (s/valid? ::content ["aa" "ee"]) 

  (s/valid? ::content ["aa" true]) 
  (s/explain ::content ["aa" true])
  

  (s/valid? ::read-result {:model/content ["ee" "rr"]})
  (s/valid? ::read-result {:model/content ["ee" "rr" true]})
  
  ;;

  )


