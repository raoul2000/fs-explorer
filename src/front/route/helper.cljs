(ns route.helper
  (:require [reitit.frontend.easy :as rfe]))

(defn href
  "Return relative url for given route. Url can be used in HTML links.
   Usage :
   ```
   [:a {:href (href ::page-1)}  \"Go to page 1\"]
   [:a {:href (href ::username-page {:username \"bob\"})}  \"Go to Bob page\"]

   ```
   "
  ([k]
   (href k nil nil))
  ([k params]
   (href k params nil))
  ([k params query]
   (rfe/href k params query)))