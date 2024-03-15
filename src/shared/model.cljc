(ns model
  (:require [clojure.spec.alpha :as s]))

;; file ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; directory or regular file name
(s/def :file/name string?)
(s/def :file/dir? boolean?)
;; full file path - platform dependent
(s/def :file/path string?)
;; caonical path id
(s/def :file/id   string?)
;; information map describing a file or a folder
(s/def :file/info (s/keys :req [:file/name :file/dir? :file/path :file/id]))

(s/def ::content (s/coll-of :file/info))


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


;; user-config ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; server port number
(s/def :user-config/server-port    (s/and int? #(< 0 % 65353)))
;; open the browser on startup ?
(s/def :user-config/open-browser   boolean?)
;; what URL open in the browser
(s/def :user-config/browse-url     string?)
;; path to the root folder for all relatives path 
(s/def :user-config/root-dir-path  string?)

(s/def :user-config/config         (s/keys :opt [:user-config/server-port
                                                 :user-config/open-browser
                                                 :user-config/browse-url
                                                 :user-config/root-dir-path]))