(ns page.config.subs
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::user-config
 (fn [db _]
   (:user-config db)))

(defn <user-config []
  @(re-frame/subscribe [::user-config]))
