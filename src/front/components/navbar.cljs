(ns components.navbar
  (:require [reagent.core :as rc]
            [route.helper :refer [home-route? about-route? explore-route? create-url-about create-url-explore create-url-home
                                  >navigate-to-about >navigate-to-explore]]
            [page.explore.subs :refer [<current-dir]]))


(defn link-explore [route-name]
  (let [current-dir (<current-dir)]
    [:li (when (explore-route? route-name) ">") [:a {:href (create-url-explore current-dir)} "Explore"]]))

;; TODO: route-name is not usable here because the component is not rerenderer
;; when the route changes.
(defn navbar-item-explore [route-name show-burger]
  (let [current-dir (<current-dir)]
    [:a.navbar-item {:href      (create-url-explore current-dir)
                     :on-click  #(reset! show-burger false)
                     :title     "explore"}
     "Explore"]))

(defn navbar
  "Render the main top Navigation Bar.   
   The *current-route* may be use to render the selected nav items"
  [current-route]
  (let [show-burger (rc/atom false)
        route-name  (get-in current-route [:data :name])]
    (tap> {:route-name route-name})
    (js/console.log "rendering navbar")
    (fn []
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
          "About"]]]])))

(defn navbar-1 [current-route]
  [:nav.is-fixed-top
   [:div.navbar-brand]])

(defn nav
  "The navigation bar component"
  [current-route]
  (let [route-name (-> current-route :data :name)]
    [:div.title "navbar "
     [:ul
      [:li (when (home-route? route-name)    ">") [:a {:href (create-url-home)}       "home"]]
      [:li (when (about-route? route-name)   ">") [:a {:href (create-url-about)}      "about"]]
      [link-explore route-name]]]))
