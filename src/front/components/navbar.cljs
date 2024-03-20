(ns components.navbar
  (:require [reagent.core :as rc]
            [route.helper :refer [about-route? explore-route? create-url-about create-url-explore config-route?
                                  create-url-config]]
            [route.subs :refer [<current-route]]
            [page.explore.subs :refer [<current-dir]]
            [components.icon :refer [quick-search-icon about-icon about-icon-selected config-icon config-icon-selected]]
            [components.search-dir :as search-dir]
            [utils :refer [cancel-event]]))


(defn navbar-item-explore
  "The nav item 'Explore' href must always refer to the current browsed dir and so this
   component is re-rendered each time the dir being explored changes."
  [route-name show-burger]
  (js/console.log "rendering navbar-item-explore")
  (let [current-dir (<current-dir)]
    [:a.navbar-item {:class     (when (explore-route? route-name) "is-underlined")
                     :href      (create-url-explore current-dir)
                     :on-click  #(reset! show-burger false)
                     :title     "explore"}
     [:img {:src     "/image/cljs-logo.png",
            :alt     "logo",
            :height  "28"}]]))

(defn navbar
  "Render the main top Navigation Bar.   
   
   It is re-rendererd :
   - when local state *show-burger* changes meaning user open/close the navbar when displayed as dropdown
   - when *current-route* changes so to underline the nav item that matches the current route"
  []
  (let [show-burger   (rc/atom false)]
    (fn []
      (let [current-route (<current-route)
            route-name    (get-in current-route [:data :name])]
        (js/console.log "rendering navbar")
        [:nav.navbar.is-fixed-top.has-shadow {:role "navigation", :aria-label "main navigation"}
         [:div.navbar-brand
          [navbar-item-explore route-name show-burger]


           ;; always visible
          #_[:a.navbar-item {:title  "search (ctrl+k)"
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
           [:a.navbar-item {:on-click (fn [event]
                                        (reset! show-burger false)
                                        (cancel-event event)
                                        (search-dir/>show))
                            :href     ""
                            :title    "quick-search (ctr + f)"}
            [:div {:style {"display" "flex"}}  quick-search-icon "Quick Search"]]

           [:a.navbar-item {:on-click #(reset! show-burger false)
                            :href     (create-url-config)
                            :title    "config"}
            (if (config-route? route-name)
              config-icon-selected
              config-icon)]

           [:a.navbar-item {:on-click #(reset! show-burger false)
                            :href     (create-url-about)
                            :title    "about"}
            (if (about-route? route-name)
              about-icon-selected
              about-icon)]]]]))))
