(ns components.navbar
  (:require [reagent.core :as rc]
            [route.helper :refer [home-route? about-route? explore-route? create-url-about create-url-explore create-url-home]]
            [page.explore.subs :refer [<current-dir]]))


(defn link-explore [route-name]
  (let [current-dir (<current-dir)]
    [:li (when (explore-route? route-name) ">") [:a {:href (create-url-explore current-dir)} "Explore"]]))

(defn navbar-item-explore [route-name]
  (let [current-dir (<current-dir)]
    [:a.navbar-item {:href (create-url-explore current-dir)
                     :title "explore"}
     "Explore"]))

(defn navbar [current-route]
  (let [show-burger (rc/atom false)
        route-name  (get-in  current-route [:data :name])]
    (fn []
      [:nav.navbar.is-fixed-top.has-shadow {:role "navigation", :aria-label "main navigation"}
       [:div.navbar-brand
        [:a.navbar-item {:href ""} ;; TODO: link to homepage : show something in homepage
         [:img {:src "/image/cljs-logo.png",
                :alt "logo",
                :height "28"}]]

        ;; always visible
        [:a.navbar-item {:title "search (ctrl+k)"
                         :href ""}
         "search"]

        ;; burger control
        [:a {:role "button", :class (str "navbar-burger " (when @show-burger "is-active")), :aria-label "menu", :aria-expanded "false"
             :on-click #(swap! show-burger not)}
         [:span {:aria-hidden "true"}]
         [:span {:aria-hidden "true"}]
         [:span {:aria-hidden "true"}]]]

       ;; responsive
       [:div {:class (str "navbar-menu " (when @show-burger "is-active"))}
        [:div.navbar-end
         [navbar-item-explore route-name]
         [:a.navbar-item {:href ""
                          :title "settings"}  "item 1"]

         [:a.navbar-item {:href (create-url-about)
                          :title "about"}
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
