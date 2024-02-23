(ns route.event
  (:require [re-frame.core :as re-frame]
            [reitit.frontend.controllers :as rfc]
            [reitit.frontend.easy :as rfe]))

;;; Effects ;;;

;; Triggering navigation from events.

(re-frame/reg-fx :push-state
                 (fn [route]
                   (apply rfe/push-state route)))

;;; Events ;;;

(re-frame/reg-event-fx ::push-state
                       (fn [_ [_ & route]]
                         {:push-state route}))

(re-frame/reg-event-db ::navigated
                       (fn [db [_ new-match]]
                         (let [old-match   (:current-route db)
                               controllers (rfc/apply-controllers (:controllers old-match) new-match)]
                           (assoc db :current-route (assoc new-match :controllers controllers)))))

