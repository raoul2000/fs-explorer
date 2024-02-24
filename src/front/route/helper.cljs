(ns route.helper
  (:require [reitit.frontend.easy :as rfe]
            [re-frame.core :as rfc]
            [page.about.route :as about]
            [page.home.route :as home]
            [page.explore.route :as explore]))

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

;; Homet ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn >navigate-to-home
  "navigate to the Home' view"
  []
  (rfc/dispatch [:route.event/push-state home/route-id]))

(defn create-url-home []
  (href home/route-id))

;; About ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn >navigate-to-about
  "navigate to the 'About' view"
  []
  (rfc/dispatch [:route.event/push-state about/route-id]))

(defn create-url-about []
  (href about/route-id))

;; Explore ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn >navigate-to-explore
  "navigate to the 'explore' view for the given dir-path"
  [dir-path]
  (rfc/dispatch [:route.event/push-state explore/route-id {:path dir-path} {:other-qparam "value"}]))

(defn create-url-explore [dir-path]
  (href explore/route-id {:path dir-path} {:extra-query-param "any value"}))

