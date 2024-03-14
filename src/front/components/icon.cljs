(ns components.icon
  (:require [phosphor.icons :as icons]))

(def app-icons {:folder-icon   (icons/icon :phosphor.fill/folder-simple)
                :file-icon     (icons/icon :phosphor.regular/file)
                :question-icon (icons/icon :phosphor.regular/question)
                :home-icon     (icons/icon :phosphor.fill/house)})

(def folder-icon (icons/render (:folder-icon app-icons)     {:size "20px" :color "#ffa03a"}))
(def file-icon   (icons/render (:file-icon app-icons)       {:size "20px"}))
(def about-icon  (icons/render (:question-icon app-icons)   {:size "20px"})) 
(def home-icon   (icons/render (:home-icon app-icons)       {:size "25px"})) 