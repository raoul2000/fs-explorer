(ns components.navbar
  (:require [reagent.core :as rc]
            [route.helper :refer [home-route? about-route? explore-route? create-url-about create-url-explore create-url-home
                                  >navigate-to-about >navigate-to-explore]]
            [page.explore.subs :refer [<current-dir]]))


(defn navbar-item-explore [route-name show-burger]
  (js/console.log "rendering navbar-item-explore")
  (let [current-dir (<current-dir)]
    [:a.navbar-item {:class     (when (explore-route? route-name) "is-underlined")
                     :href      (create-url-explore current-dir)
                     :on-click  #(reset! show-burger false)
                     :title     "explore"}
     "Explore"]))

(defn navbar
  "Render the main top Navigation Bar.   
   The *current-route* may be use to render the selected nav items"
  []
  (let [show-burger (rc/atom false)]
    (fn [current-route]
      (js/console.log "rendering navbar")
      (let [route-name (get-in current-route [:data :name])]
        [:nav.navbar.is-fixed-top.has-shadow {:role "navigation", :aria-label "main navigation"}
         [:div.navbar-brand
          [:a.navbar-item {:href ""}
           [:img {:src     "/image/cljs-logo.png",
                  :alt     "logo",
                  :height  "28"}]]

                  ;; always visible
          [:a.navbar-item {:title  "search (ctrl+k)"
                           :href   ""}
           "search"]

                  ;; burger control - displayed driven by *show-burger* r/atom
          [:a {:role "button", :class (str "navbar-burger " (when @show-burger "is-active")), :aria-label "menu", :aria-expanded "false"
               :on-click #(swap! show-burger not)}
           [:span {:aria-hidden "true"}]
           [:span {:aria-hidden "true"}]
           [:span {:aria-hidden "true"}]]]

                 ;; responsive
         [:div {:class (str "navbar-menu " (when @show-burger "is-active"))}
          [:div.navbar-end

                   ;; Nav items must force burger control to close when they are clicked
           [navbar-item-explore route-name show-burger]
           [:a.navbar-item {:href ""
                            :title "settings"}
            "item 1"]

           [:a.navbar-item {:class    (when (about-route? route-name) "is-underlined")
                            :on-click #(reset! show-burger false)
                            :href     (create-url-about)
                            :title    "about"}
            "About"]]]]))))
