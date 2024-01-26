(ns user
  (:require [portal.api :as p]))


;; see https://github.com/djblue/portal#api for setup

(def p (p/open {:launcher :vs-code}))
(add-tap #'p/submit)

(tap> "hello")
(p/clear)

