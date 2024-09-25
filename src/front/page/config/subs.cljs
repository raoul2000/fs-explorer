(ns page.config.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::config
 (fn [db _]
   (:config db)))

(defn <config []
  @(re-frame/subscribe [::config]))
