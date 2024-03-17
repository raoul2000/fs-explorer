(ns components.icon
  (:require [phosphor.icons :as icons]))

(def app-icons {:folder-icon            (icons/icon :phosphor.fill/folder-simple)
                :file-icon              (icons/icon :phosphor.regular/file)
                :question-icon          (icons/icon :phosphor.regular/question)
                :question-icon-selected (icons/icon :phosphor.fill/question)
                :home-icon              (icons/icon :phosphor.fill/house)
                :config-icon            (icons/icon :phosphor.regular/gear)
                :config-icon-selected   (icons/icon :phosphor.fill/gear)
                :quick-search-icon      (icons/icon :phosphor.regular/magnifying-glass)
                })

;; nanbar icons

(def about-icon           (icons/render (:question-icon app-icons)            {:size "20px"}))
(def about-icon-selected  (icons/render (:question-icon-selected app-icons)   {:size "20px"}))

(def config-icon          (icons/render (:config-icon app-icons)              {:size "20px"}))
(def config-icon-selected (icons/render (:config-icon-selected app-icons)     {:size "20px"})) 

(def quick-search-icon    (icons/render (:quick-search-icon app-icons)              {:size "20px"}))

;; explorer view icons

(def folder-icon          (icons/render (:folder-icon app-icons)              {:size "20px" :color "#ffa03a"}))
(def file-icon            (icons/render (:file-icon app-icons)                {:size "20px"}))
(def home-icon            (icons/render (:home-icon app-icons)                {:size "25px"}))
