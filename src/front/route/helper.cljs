(ns route.helper
  (:require [reitit.frontend.easy :as rfe]
            [re-frame.core :as rfc]))

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

;; Home ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn >navigate-to-home
  "navigate to the Home' view"
  []
  (rfc/dispatch [:route.event/push-state :route/home]))

(defn create-url-home []
  (href :route/home))

(def home-route? (partial = :route/home))

;; About ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn >navigate-to-about
  "navigate to the 'About' view"
  []
  (rfc/dispatch [:route.event/push-state :route/about]))

(defn create-url-about []
  (href :route/about))

(def about-route? (partial = :route/about))

;; Explore ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn >navigate-to-explore
  "navigate to the 'explore' view for the given dir-path"
  [dir-path]
  (rfc/dispatch [:route.event/push-state :route/explore{:dir dir-path}]))

(defn create-url-explore [dir-path]
  (href :route/explore {} {:dir dir-path}))

(def explore-route? (partial = :route/explore))